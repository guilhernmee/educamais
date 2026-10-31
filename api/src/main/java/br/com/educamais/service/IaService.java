package br.com.educamais.service;

import br.com.educamais.model.InsightIa;
import br.com.educamais.repository.HabilidadeRepository;
import br.com.educamais.repository.InsightIaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * IA pedagógica do Educa+ (desenvolvida pelo grupo, parte do sistema).
 * Transforma padrões de erros e acertos em insights para professores e coordenação.
 * A detecção no jogo (2 erros na mesma habilidade) acontece no app; aqui ela vira registro e aviso.
 */
@Service
@RequiredArgsConstructor
public class IaService {

    private final InsightIaRepository insights;
    private final HabilidadeRepository habilidades;

    /** Explicação do erro mais comum de cada habilidade, usada no texto dos insights. */
    private static final Map<String, String> PADRAO_DE_ERRO = Map.of(
            "multiplicação", "O erro mais comum é somar os números em vez de juntar os grupos (por exemplo, 3 × 4 = 7).",
            "frações", "Trocam o número de cima pelo de baixo. Atividades com pizza e barras costumam ajudar.",
            "subtração", "Erram quando precisam tirar mais do que 5. Contar para trás com os dedos ajuda.",
            "interpretação de texto", "Respondem antes de reler o texto. Pedir para sublinhar a pista ajuda.",
            "ortografia", "Trocam letras de som parecido (s/ss, r/rr). Ditados curtos em jogo ajudam.",
            "leitura de palavras", "Leem sílaba por sílaba sem juntar. Jogos de formar palavras ajudam.");

    public static String padraoDeErro(String habilidade) {
        return PADRAO_DE_ERRO.getOrDefault(habilidade,
                "Os erros se repetem no mesmo tipo de desafio. Vale retomar o conteúdo com exemplos visuais.");
    }

    /** Aviso imediato para o professor quando o jogo detecta dificuldade em um aluno. */
    public void alunoComDificuldade(Integer idAluno, String nomeAluno, Integer idTurma, String habilidade, int erros) {
        salvar("PROFESSOR", idTurma, idAluno, idHabilidade(habilidade), "DIFICULDADE",
                nomeAluno + " está com dificuldade em " + habilidade,
                "Errou " + erros + " vezes o mesmo tipo de desafio. O tutor deixou os próximos desafios mais fáceis e ofereceu um treino especial.");
    }

    public void alunoSuperou(Integer idAluno, String nomeAluno, Integer idTurma, String habilidade) {
        salvar("PROFESSOR", idTurma, idAluno, idHabilidade(habilidade), "AVANCO",
                nomeAluno + " superou a dificuldade em " + habilidade,
                "Terminou o treino especial com no máximo 1 erro. A trilha voltou ao nível normal.");
    }

    /**
     * Grava o insight sem duplicar: se já existe um igual (mesmo público, turma, aluno, tipo e título,
     * ignorando os números) nos últimos 30 dias, só atualiza os números enquanto ainda é novo.
     */
    public void salvar(String publico, Integer idTurma, Integer idAluno, Integer idHabilidade, String tipo, String titulo, String texto) {
        String chave = titulo.replaceAll("\\d+", "#");
        LocalDateTime limite = LocalDateTime.now().minusDays(30);
        Optional<InsightIa> existente = insights.findByPublicoOrderByIdInsightDesc(publico).stream()
                .filter(i -> Objects.equals(i.getIdTurma(), idTurma) && Objects.equals(i.getIdAluno(), idAluno)
                        && i.getTipo().equals(tipo) && i.getTitulo().replaceAll("\\d+", "#").equals(chave)
                        && (i.getDataGeracao() == null || i.getDataGeracao().isAfter(limite)))
                .findFirst();
        if (existente.isPresent()) {
            InsightIa i = existente.get();
            if ("NOVO".equals(i.getSituacao()) && (!i.getTitulo().equals(titulo) || !i.getTexto().equals(texto))) {
                i.setTitulo(titulo);
                i.setTexto(texto);
                insights.save(i);
            }
            return;
        }
        InsightIa i = new InsightIa();
        i.setPublico(publico);
        i.setIdTurma(idTurma);
        i.setIdAluno(idAluno);
        i.setIdHabilidade(idHabilidade);
        i.setTipo(tipo);
        i.setTitulo(titulo);
        i.setTexto(texto);
        insights.save(i);
    }

    public List<InsightIa> listar(String publico) {
        return insights.findByPublicoOrderByIdInsightDesc(publico).stream()
                .filter(i -> !"DISPENSADO".equals(i.getSituacao())).toList();
    }

    public Integer idHabilidade(String nome) {
        return nome == null ? null : habilidades.findFirstByNome(nome).map(h -> h.getIdHabilidade()).orElse(null);
    }
}
