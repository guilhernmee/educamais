package br.com.educamais.service;

import br.com.educamais.model.*;
import br.com.educamais.repository.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Trilhas (níveis) e questões criadas por professores. */
@Service
@RequiredArgsConstructor
public class ConteudoService {

    private final DisciplinaRepository disciplinas;
    private final ConteudoRepository conteudos;
    private final AtividadeRepository atividades;
    private final QuestaoRepository questoes;
    private final AlternativaRepository alternativas;
    private final HabilidadeRepository habilidades;
    private final UsuarioRepository usuarios;

    /**
     * Reordena a trilha de uma ilha num ano. Cada região mantém a quantidade de níveis que tinha;
     * os níveis são distribuídos nas regiões na nova ordem.
     */
    @Transactional
    public void reordenar(int ano, String codigo, List<Integer> ids) {
        List<Conteudo> regioes = regioes(ano, codigo);
        int pos = 0;
        for (Conteudo r : regioes) {
            int tamanho = atividades.findByIdConteudoOrderByOrdemAsc(r.getIdConteudo()).size();
            for (int k = 0; k < tamanho && pos < ids.size(); k++) {
                Atividade a = atividade(ids.get(pos++));
                a.setIdConteudo(r.getIdConteudo());
                a.setOrdem(k + 1);
                atividades.save(a);
            }
        }
    }

    @Transactional
    public void editarNivel(Integer id, String titulo, Integer dificuldade) {
        Atividade a = atividade(id);
        if (titulo != null && !titulo.isBlank()) {
            a.setTitulo(titulo.trim());
        }
        if (dificuldade != null) {
            a.setDificuldade(Math.max(1, Math.min(3, dificuldade)));
        }
        atividades.save(a);
    }

    /** Novo nível no fim da última região da ilha. */
    @Transactional
    public Map<String, Object> criarNivel(int ano, String codigo, String titulo, Integer dificuldade, String tipoMinijogo, Integer idAutor) {
        if (titulo == null || titulo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dê um nome ao nível");
        }
        Disciplina d = disciplina(codigo);
        List<Conteudo> regioes = regioes(ano, codigo);
        Conteudo ultima;
        if (regioes.isEmpty()) {
            ultima = new Conteudo();
            ultima.setIdDisciplina(d.getIdDisciplina());
            ultima.setTitulo("Nova região");
            ultima.setMaterial("Criada pela área do professor");
            ultima.setAnoEscolar(ano);
            ultima.setOrdem(1);
            ultima = conteudos.saveAndFlush(ultima);
        } else {
            ultima = regioes.get(regioes.size() - 1);
        }
        List<Atividade> existentes = atividades.findByIdConteudoOrderByOrdemAsc(ultima.getIdConteudo());
        Atividade a = new Atividade();
        a.setIdConteudo(ultima.getIdConteudo());
        a.setTitulo(titulo.trim());
        a.setOrdem(existentes.isEmpty() ? 1 : existentes.get(existentes.size() - 1).getOrdem() + 1);
        a.setDificuldade(dificuldade == null ? 2 : Math.max(1, Math.min(3, dificuldade)));
        a.setIdAutor(idAutor);
        if ("mat".equals(codigo) && tipoMinijogo != null) {
            a.setTipoMinijogo(tipoMinijogo);
            String hab = Catalogo.HABILIDADE_DO_MINIJOGO.get(tipoMinijogo);
            habilidades.findFirstByIdDisciplinaAndNome(d.getIdDisciplina(), hab).ifPresent(h -> a.setIdHabilidade(h.getIdHabilidade()));
        }
        atividades.saveAndFlush(a);
        return BootstrapService.mapa("id", a.getIdAtividade(), "title", a.getTitulo(), "kind", a.getTipoMinijogo(), "diff", a.getDificuldade(),
                "regiao", ultima.getTitulo(), "idRegiao", ultima.getIdConteudo(), "habilidade", null, "stars", 0);
    }

    public record QuestaoDTO(Integer idAtividade, String enunciado, String tipo, List<String> alternativas, Integer correta,
                             String dica, Integer idAutor) {
    }

    /** Questão de professor entra em revisão; feita pela coordenação já sai publicada. */
    @Transactional
    public Map<String, Object> criarQuestao(QuestaoDTO dto) {
        Atividade a = atividade(dto.idAtividade());
        Usuario autor = usuarios.findById(dto.idAutor())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrado"));
        List<String> opcoes = new ArrayList<>();
        for (String s : dto.alternativas() == null ? List.<String>of() : dto.alternativas()) {
            if (s != null && !s.isBlank()) {
                opcoes.add(s.trim());
            }
        }
        if (dto.enunciado() == null || dto.enunciado().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escreva a pergunta");
        }
        if (opcoes.size() < 2 || dto.correta() == null || dto.correta() < 0 || dto.correta() >= dto.alternativas().size()
                || dto.alternativas().get(dto.correta()) == null || dto.alternativas().get(dto.correta()).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coloque pelo menos duas opções e marque a certa");
        }
        String certa = dto.alternativas().get(dto.correta()).trim();
        Questao q = new Questao();
        q.setIdAtividade(a.getIdAtividade());
        q.setEnunciado(dto.enunciado().trim());
        q.setOrdem(questoes.findByIdAtividade(a.getIdAtividade()).stream().mapToInt(Questao::getOrdem).max().orElse(0) + 1);
        q.setTipo("tf".equals(dto.tipo()) ? "VERDADEIRO_FALSO" : "MULTIPLA_ESCOLHA");
        q.setDica(dto.dica());
        q.setIdHabilidade(a.getIdHabilidade());
        q.setIdAutor(autor.getIdUsuario());
        q.setSituacao("COORDENADOR".equals(autor.getPerfil()) ? "PUBLICADA" : "EM_REVISAO");
        questoes.saveAndFlush(q);
        int indiceCerta = 0;
        for (int i = 0; i < opcoes.size(); i++) {
            Alternativa alt = new Alternativa();
            alt.setIdQuestao(q.getIdQuestao());
            alt.setTexto(opcoes.get(i));
            alt.setCorreta(opcoes.get(i).equals(certa));
            if (alt.getCorreta()) {
                indiceCerta = i;
            }
            alternativas.save(alt);
        }
        Conteudo c = conteudos.findById(a.getIdConteudo()).orElseThrow();
        String codigo = disciplinas.findById(c.getIdDisciplina()).map(Disciplina::getCodigo).orElse("");
        return BootstrapService.mapa("id", q.getIdQuestao(), "subj", codigo, "idAtividade", a.getIdAtividade(), "ano", c.getAnoEscolar(),
                "prompt", q.getEnunciado(), "options", opcoes, "answer", indiceCerta, "hint", q.getDica(), "author", autor.getNome(),
                "status", Catalogo.situacaoQuestao(q.getSituacao()), "diff", a.getDificuldade(), "type", q.getTipo());
    }

    @Transactional
    public void situacaoQuestao(Integer id, String situacao) {
        if (!List.of("EM_REVISAO", "PUBLICADA", "RECUSADA").contains(situacao)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Situação inválida");
        }
        Questao q = questoes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada"));
        q.setSituacao(situacao);
        questoes.save(q);
    }

    // ------------------------------------------------------------------

    private List<Conteudo> regioes(int ano, String codigo) {
        return conteudos.findByIdDisciplinaAndAnoEscolarOrderByOrdemAsc(disciplina(codigo).getIdDisciplina(), ano);
    }

    private Disciplina disciplina(String codigo) {
        return disciplinas.findByCodigo(codigo).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
    }

    private Atividade atividade(Integer id) {
        return atividades.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nível não encontrado"));
    }
}
