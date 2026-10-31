package br.com.educamais.service;

import br.com.educamais.model.*;
import br.com.educamais.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Tudo que a criança grava jogando: partidas, dificuldades, compras, personagem e conversa com o tutor. */
@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunos;
    private final UsuarioRepository usuarios;
    private final TurmaRepository turmas;
    private final RealizacaoRepository realizacoes;
    private final RespostaRepository respostas;
    private final AlternativaRepository alternativas;
    private final AtividadeRepository atividades;
    private final DisciplinaRepository disciplinas;
    private final HabilidadeRepository habilidades;
    private final DificuldadeDetectadaRepository dificuldades;
    private final ItemLojaRepository itens;
    private final MensagemTutorRepository mensagens;
    private final AlunoDisciplinaRepository alunoDisciplinas;
    private final IaService ia;
    private final EntityManager em;

    public record RespostaDTO(Integer idQuestao, String texto, Boolean correta, Integer tentativas) {
    }

    public record PartidaDTO(Integer idAtividade, String modo, Integer estrelas, Integer acertos, Integer erros,
                             Integer dicas, Integer percentual, Integer segundos, List<RespostaDTO> respostas) {
    }

    /** Grava a partida como ENVIADA; o gatilho do banco credita 10 estrelas por estrela conquistada. */
    @Transactional
    public Map<String, Object> registrarPartida(Integer idAluno, PartidaDTO p) {
        aluno(idAluno);
        if (p.idAtividade() == null || atividades.findById(p.idAtividade()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nível não encontrado");
        }
        LocalDateTime fim = LocalDateTime.now();
        Realizacao r = new Realizacao();
        r.setIdAluno(idAluno);
        r.setIdAtividade(p.idAtividade());
        r.setModo("REFORCO".equals(p.modo()) ? "REFORCO" : "TRILHA");
        r.setDataInicio(fim.minusSeconds(p.segundos() == null ? 60 : Math.max(1, p.segundos())));
        r.setDataEnvio(fim);
        r.setSituacao("ENVIADA");
        r.setEstrelas(limite(p.estrelas(), 0, 3));
        r.setAcertos(limite(p.acertos(), 0, 999));
        r.setErros(limite(p.erros(), 0, 999));
        r.setDicasUsadas(limite(p.dicas(), 0, 999));
        r.setPercentualAcertos(BigDecimal.valueOf(limite(p.percentual(), 0, 100)));
        realizacoes.saveAndFlush(r);
        if (p.respostas() != null) {
            for (RespostaDTO dto : p.respostas()) {
                Resposta resp = new Resposta();
                resp.setIdRealizacao(r.getIdRealizacao());
                resp.setIdQuestao(dto.idQuestao());
                resp.setCorreta(Boolean.TRUE.equals(dto.correta()));
                resp.setTentativas(Math.max(1, dto.tentativas() == null ? 1 : dto.tentativas()));
                alternativas.findByIdQuestaoOrderByIdAlternativaAsc(dto.idQuestao()).stream()
                        .filter(a -> a.getTexto().equals(dto.texto())).findFirst()
                        .ifPresent(a -> resp.setIdAlternativa(a.getIdAlternativa()));
                respostas.save(resp);
            }
        }
        return BootstrapService.mapa("saldo", alunos.saldo(idAluno), "ganho", r.getEstrelas() * 10, "idRealizacao", r.getIdRealizacao());
    }

    /** O jogo percebeu 2 erros na mesma habilidade: registra e avisa o professor. */
    @Transactional
    public Map<String, Object> registrarDificuldade(Integer idAluno, String codigoDisciplina, String nomeHabilidade, Integer erros) {
        Aluno a = aluno(idAluno);
        Habilidade h = habilidade(codigoDisciplina, nomeHabilidade);
        int qtd = erros == null ? 2 : Math.max(1, erros);
        DificuldadeDetectada d = dificuldades.findFirstByIdAlunoAndIdHabilidadeAndResolvidaFalse(idAluno, h.getIdHabilidade())
                .orElseGet(() -> {
                    DificuldadeDetectada nova = new DificuldadeDetectada();
                    nova.setIdAluno(idAluno);
                    nova.setIdHabilidade(h.getIdHabilidade());
                    nova.setQtdErros(0);
                    return nova;
                });
        d.setQtdErros(d.getQtdErros() + qtd);
        dificuldades.save(d);
        ia.alunoComDificuldade(idAluno, nome(idAluno), a.getIdTurma(), h.getNome(), d.getQtdErros());
        return BootstrapService.mapa("habilidade", h.getNome(), "erros", d.getQtdErros());
    }

    /** O aluno passou no treino especial: a dificuldade vira superada. */
    @Transactional
    public Map<String, Object> resolverDificuldade(Integer idAluno, String nomeHabilidade) {
        Aluno a = aluno(idAluno);
        habilidades.findFirstByNome(nomeHabilidade)
                .flatMap(h -> dificuldades.findFirstByIdAlunoAndIdHabilidadeAndResolvidaFalse(idAluno, h.getIdHabilidade()))
                .ifPresent(d -> {
                    d.setResolvida(true);
                    d.setDataResolucao(LocalDateTime.now());
                    dificuldades.save(d);
                    ia.alunoSuperou(idAluno, nome(idAluno), a.getIdTurma(), nomeHabilidade);
                });
        return BootstrapService.mapa("ok", true);
    }

    /**
     * Compra pela procedure sp_comprar_item, que confere saldo e posse dentro de uma transação do banco.
     * Por isso este método não abre transação própria.
     */
    public Map<String, Object> comprar(Integer idAluno, String parte, String codigo) {
        aluno(idAluno);
        String categoria = Catalogo.CATEGORIA_BANCO.get(parte);
        ItemLoja item = itens.findByCategoriaAndCodigo(categoria, codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado na loja"));
        StoredProcedureQuery sp = em.createStoredProcedureQuery("sp_comprar_item")
                .registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(2, Integer.class, ParameterMode.IN)
                .setParameter(1, idAluno)
                .setParameter(2, item.getIdItem());
        sp.execute();
        return BootstrapService.mapa("saldo", alunos.saldo(idAluno), "item", item.getNome());
    }

    /** Salva cores e o item usado em cada parte do personagem. */
    @Transactional
    public Map<String, Object> salvarPersonagem(Integer idAluno, Map<String, String> eq) {
        aluno(idAluno);
        alunos.salvarCores(idAluno, eq.get("skin"), eq.get("hairC"));
        alunos.equipar(idAluno, eq.getOrDefault("hair", ""), eq.getOrDefault("shirt", ""), eq.getOrDefault("acc", ""),
                eq.getOrDefault("bag", ""), eq.getOrDefault("pet", ""));
        return BootstrapService.mapa("ok", true);
    }

    @Transactional
    public Map<String, Object> mensagem(Integer idAluno, String autor, String texto) {
        aluno(idAluno);
        MensagemTutor m = new MensagemTutor();
        m.setIdAluno(idAluno);
        m.setAutor("ALUNO".equals(autor) ? "ALUNO" : "TUTOR");
        m.setTexto(texto.length() > 1000 ? texto.substring(0, 1000) : texto);
        mensagens.save(m);
        return BootstrapService.mapa("ok", true);
    }

    /** Professor liga ou desliga uma disciplina (ilha) para o aluno. */
    @Transactional
    public Map<String, Object> definirDisciplina(Integer idAluno, String codigo, Boolean habilitada, Integer idProfessor) {
        aluno(idAluno);
        Disciplina d = disciplinas.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
        AlunoDisciplina ad = alunoDisciplinas.findById(new AlunoDisciplina.Chave(idAluno, d.getIdDisciplina())).orElseGet(() -> {
            AlunoDisciplina nova = new AlunoDisciplina();
            nova.setIdAluno(idAluno);
            nova.setIdDisciplina(d.getIdDisciplina());
            return nova;
        });
        ad.setHabilitada(Boolean.TRUE.equals(habilitada));
        ad.setIdProfessorAlteracao(idProfessor);
        ad.setDataAlteracao(LocalDateTime.now());
        alunoDisciplinas.save(ad);
        return BootstrapService.mapa("ok", true);
    }

    /** Cadastra um aluno novo na turma, já com os itens iniciais do personagem. */
    @Transactional
    public Integer cadastrar(String nome, String email, Integer idTurma) {
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do aluno");
        }
        turmas.findById(idTurma).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));
        String mail = email == null || email.isBlank()
                ? nome.trim().toLowerCase().replaceAll("[^a-z]+", ".") + System.currentTimeMillis() % 100000 + "@aluno.escolahorizonte.edu.br"
                : email.trim().toLowerCase();
        if (usuarios.findByEmail(mail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe alguém com este e-mail");
        }
        Usuario u = new Usuario();
        u.setNome(nome.trim());
        u.setEmail(mail);
        u.setPerfil("ALUNO");
        usuarios.saveAndFlush(u);
        Aluno a = new Aluno();
        a.setIdUsuario(u.getIdUsuario());
        a.setIdTurma(idTurma);
        alunos.saveAndFlush(a);
        alunos.darItensIniciais(u.getIdUsuario());
        return u.getIdUsuario();
    }

    // ------------------------------------------------------------------

    private Aluno aluno(Integer id) {
        return alunos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
    }

    private String nome(Integer id) {
        return usuarios.findById(id).map(Usuario::getNome).orElse("Aluno");
    }

    /** Acha a habilidade; se o jogo mandar uma que ainda não existe, cria. */
    private Habilidade habilidade(String codigoDisciplina, String nome) {
        Disciplina d = disciplinas.findByCodigo(codigoDisciplina)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
        return habilidades.findFirstByIdDisciplinaAndNome(d.getIdDisciplina(), nome)
                .or(() -> habilidades.findFirstByNome(nome))
                .orElseGet(() -> {
                    Habilidade h = new Habilidade();
                    h.setIdDisciplina(d.getIdDisciplina());
                    h.setNome(nome);
                    return habilidades.save(h);
                });
    }

    private static int limite(Integer v, int min, int max) {
        return v == null ? min : Math.max(min, Math.min(max, v));
    }
}
