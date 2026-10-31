package br.com.educamais.service;

import br.com.educamais.model.*;
import br.com.educamais.repository.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Monta, numa resposta só, tudo que as telas de cada perfil precisam:
 * a criança recebe a trilha do ano dela; professor e coordenação recebem turmas, alunos,
 * indicadores e os insights da IA (gerados aqui a partir dos dados).
 */
@Service
@RequiredArgsConstructor
public class BootstrapService {

    private static final String[] MESES = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final UsuarioRepository usuarios;
    private final TurmaRepository turmas;
    private final AlunoRepository alunos;
    private final DisciplinaRepository disciplinas;
    private final DisciplinaAnoRepository disciplinaAnos;
    private final DisciplinaTurmaRepository disciplinaTurmas;
    private final AlunoDisciplinaRepository alunoDisciplinas;
    private final HabilidadeRepository habilidades;
    private final ConteudoRepository conteudos;
    private final AtividadeRepository atividades;
    private final QuestaoRepository questoes;
    private final AlternativaRepository alternativas;
    private final RealizacaoRepository realizacoes;
    private final ItemLojaRepository itens;
    private final AlunoItemRepository alunoItens;
    private final DificuldadeDetectadaRepository dificuldades;
    private final MensagemTutorRepository mensagens;
    private final IaService ia;

    @Value("${educa.escola:Escola Horizonte}")
    private String escola;

    /** Tudo carregado da base, organizado para consulta rápida. */
    private static class Dados {
        final LocalDateTime agora = LocalDateTime.now();
        final Map<Integer, Disciplina> disc = new LinkedHashMap<>();
        final Map<Integer, String> cod = new HashMap<>();
        final Map<String, List<Integer>> anos = new LinkedHashMap<>();
        final Map<Integer, Conteudo> cont = new HashMap<>();
        final Map<Integer, Habilidade> hab = new HashMap<>();
        final Map<Integer, Atividade> atv = new HashMap<>();
        final Map<String, List<Atividade>> trilha = new HashMap<>();
        final Map<Integer, Turma> turma = new LinkedHashMap<>();
        final Map<Integer, Integer> profDaTurma = new HashMap<>();
        final Map<Integer, Set<Integer>> turmasDoProf = new HashMap<>();
        final Map<Integer, Set<String>> disciplinasDoProf = new HashMap<>();
        final Map<Integer, Usuario> usuario = new HashMap<>();
        final Map<Integer, Aluno> aluno = new LinkedHashMap<>();
        final Map<Integer, List<Realizacao>> realiz = new HashMap<>();
        final Map<Integer, List<DificuldadeDetectada>> difs = new HashMap<>();
        final Map<Integer, Map<String, Boolean>> desligadas = new HashMap<>();
        final Map<Integer, ItemLoja> item = new HashMap<>();
        final Map<Integer, List<AlunoItem>> itensAluno = new HashMap<>();
        final Map<Integer, Long> ganhos = new HashMap<>();
    }

    private Dados carregarDados() {
        Dados d = new Dados();
        disciplinas.findAll().forEach(x -> {
            d.disc.put(x.getIdDisciplina(), x);
            d.cod.put(x.getIdDisciplina(), x.getCodigo());
            d.anos.put(x.getCodigo(), new ArrayList<>());
        });
        disciplinaAnos.findAll().forEach(x -> d.anos.get(d.cod.get(x.getIdDisciplina())).add(x.getAnoEscolar()));
        d.anos.values().forEach(Collections::sort);
        conteudos.findAll().forEach(x -> d.cont.put(x.getIdConteudo(), x));
        habilidades.findAll().forEach(x -> d.hab.put(x.getIdHabilidade(), x));
        List<Atividade> todas = new ArrayList<>(atividades.findAll());
        todas.sort(Comparator.comparing((Atividade a) -> d.cont.get(a.getIdConteudo()).getOrdem())
                .thenComparing(Atividade::getOrdem).thenComparing(Atividade::getIdAtividade));
        for (Atividade a : todas) {
            Conteudo c = d.cont.get(a.getIdConteudo());
            d.atv.put(a.getIdAtividade(), a);
            d.trilha.computeIfAbsent(c.getAnoEscolar() + ":" + d.cod.get(c.getIdDisciplina()), k -> new ArrayList<>()).add(a);
        }
        turmas.findAll().forEach(t -> d.turma.put(t.getIdTurma(), t));
        for (DisciplinaTurma dt : disciplinaTurmas.findAll()) {
            String codigo = d.cod.get(dt.getIdDisciplina());
            d.turmasDoProf.computeIfAbsent(dt.getIdProfessor(), k -> new TreeSet<>()).add(dt.getIdTurma());
            d.disciplinasDoProf.computeIfAbsent(dt.getIdProfessor(), k -> new HashSet<>()).add(codigo);
            if (!"ing".equals(codigo) || !d.profDaTurma.containsKey(dt.getIdTurma())) {
                d.profDaTurma.put(dt.getIdTurma(), dt.getIdProfessor());
            }
        }
        usuarios.findAll().forEach(u -> d.usuario.put(u.getIdUsuario(), u));
        alunos.findAll().forEach(a -> d.aluno.put(a.getIdUsuario(), a));
        realizacoes.findBySituacao("ENVIADA").forEach(r -> d.realiz.computeIfAbsent(r.getIdAluno(), k -> new ArrayList<>()).add(r));
        dificuldades.findAll().forEach(x -> d.difs.computeIfAbsent(x.getIdAluno(), k -> new ArrayList<>()).add(x));
        alunoDisciplinas.findAll().forEach(x -> d.desligadas.computeIfAbsent(x.getIdAluno(), k -> new HashMap<>())
                .put(d.cod.get(x.getIdDisciplina()), Boolean.TRUE.equals(x.getHabilitada())));
        itens.findAll().forEach(i -> d.item.put(i.getIdItem(), i));
        alunoItens.findAll().forEach(x -> d.itensAluno.computeIfAbsent(x.getIdAluno(), k -> new ArrayList<>()).add(x));
        for (Object[] linha : alunos.ganhosPorAluno()) {
            d.ganhos.put(((Number) linha[0]).intValue(), ((Number) linha[1]).longValue());
        }
        return d;
    }

    // ------------------------------------------------------------------ entrada

    @Transactional
    public Map<String, Object> carregar(Integer idUsuario) {
        Usuario u = usuarios.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        Dados d = carregarDados();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("usuario", mapa("id", str(u.getIdUsuario()), "nome", u.getNome(), "email", u.getEmail(), "perfil", u.getPerfil()));
        out.put("escola", escola);
        out.put("hoje", Catalogo.capitalizar(d.agora.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT_BR))));
        out.put("meses", ultimosMeses(d).stream().map(m -> MESES[m.getMonthValue() - 1]).toList());
        out.put("anosDisciplina", d.anos);

        if ("ALUNO".equals(u.getPerfil())) {
            Aluno a = d.aluno.get(u.getIdUsuario());
            if (a == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno sem turma cadastrada");
            }
            int ano = d.turma.get(a.getIdTurma()).getAnoEscolar();
            out.put("trilhas", Map.of(str(ano), trilhasDoAno(d, ano)));
            out.put("custom", questoesDosProfessores(d, a.getIdTurma()));
            out.put("kid", crianca(d, a));
            return out;
        }

        boolean coordenador = "COORDENADOR".equals(u.getPerfil());
        Set<Integer> minhas = coordenador ? d.turma.keySet() : d.turmasDoProf.getOrDefault(u.getIdUsuario(), Set.of());
        Map<String, Object> trilhas = new TreeMap<>();
        d.cont.values().stream().map(Conteudo::getAnoEscolar).distinct().forEach(ano -> trilhas.put(str(ano), trilhasDoAno(d, ano)));
        out.put("trilhas", trilhas);
        out.put("custom", questoesDosProfessores(d, null));
        out.put("turmas", d.turma.values().stream().map(t -> mapa("id", str(t.getIdTurma()), "name", t.getNome(),
                "ano", t.getAnoEscolar(), "prof", str(d.profDaTurma.get(t.getIdTurma())),
                "n", d.aluno.values().stream().filter(a -> a.getIdTurma().equals(t.getIdTurma())).count())).toList());
        out.put("minhasTurmas", minhas.stream().map(BootstrapService::str).toList());
        out.put("profs", usuarios.findByPerfil("PROFESSOR").stream().map(p -> professor(d, p)).toList());

        List<Map<String, Object>> lista = d.aluno.values().stream()
                .filter(a -> minhas.contains(a.getIdTurma())).map(a -> resumoAluno(d, a)).toList();
        out.put("alunos", lista);
        Map<String, List<Integer>> series = new LinkedHashMap<>();
        for (Integer tid : minhas) {
            series.put(str(tid), serie(d, tid));
        }
        out.put("series", series);

        if (coordenador) {
            gerarInsightsEscola(d, lista, series);
            out.put("insights", ia.listar("COORDENADOR").stream().map(BootstrapService::insight).toList());
        } else {
            gerarInsightsProfessor(d, minhas, lista);
            Set<String> ids = minhas.stream().map(BootstrapService::str).collect(Collectors.toSet());
            out.put("insights", ia.listar("PROFESSOR").stream()
                    .filter(i -> ids.contains(str(i.getIdTurma()))).map(BootstrapService::insight).toList());
        }
        return out;
    }

    /** Resumo de um aluno no mesmo formato da lista (usado depois de cadastrar um aluno novo). */
    @Transactional(readOnly = true)
    public Map<String, Object> resumo(Integer idAluno) {
        Dados d = carregarDados();
        return resumoAluno(d, d.aluno.get(idAluno));
    }

    /** Detalhes do perfil do aluno na área do professor. */
    @Transactional(readOnly = true)
    public Map<String, Object> detalhe(Integer idAluno) {
        Dados d = carregarDados();
        if (!d.aluno.containsKey(idAluno)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado");
        }
        List<Realizacao> rs = d.realiz.getOrDefault(idAluno, List.of());
        Map<Integer, Integer> melhor = melhorPorAtividade(rs);
        List<String> dominados = melhor.entrySet().stream().filter(e -> e.getValue() == 3)
                .map(e -> d.atv.get(e.getKey()).getTitulo()).distinct().limit(8).toList();
        List<String> superadas = d.difs.getOrDefault(idAluno, List.of()).stream().filter(x -> Boolean.TRUE.equals(x.getResolvida()))
                .map(x -> d.hab.get(x.getIdHabilidade()).getNome()).distinct().toList();
        return mapa("skills", habilidadesDoAluno(d, rs), "dominados", dominados, "superadas", superadas,
                "estrelas", d.ganhos.getOrDefault(idAluno, 0L));
    }

    // ------------------------------------------------------------------ trilhas e questões

    private Map<String, Object> trilhasDoAno(Dados d, int ano) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (String codigo : d.anos.keySet()) {
            List<Atividade> l = d.trilha.get(ano + ":" + codigo);
            if (l != null && !l.isEmpty()) {
                m.put(codigo, l.stream().map(a -> nivel(d, a)).toList());
            }
        }
        return m;
    }

    private Map<String, Object> nivel(Dados d, Atividade a) {
        Conteudo c = d.cont.get(a.getIdConteudo());
        Habilidade h = a.getIdHabilidade() == null ? null : d.hab.get(a.getIdHabilidade());
        return mapa("id", a.getIdAtividade(), "title", a.getTitulo(), "kind", a.getTipoMinijogo(), "diff", a.getDificuldade(),
                "regiao", c.getTitulo(), "idRegiao", c.getIdConteudo(), "habilidade", h == null ? null : h.getNome(), "stars", 0);
    }

    /** Questões criadas por professores. Para o aluno: publicadas, ou em revisão feitas pelos professores da turma dele. */
    private List<Map<String, Object>> questoesDosProfessores(Dados d, Integer turmaDoAluno) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Questao q : questoes.findByIdAutorIsNotNull()) {
            if (turmaDoAluno != null) {
                boolean publicada = "PUBLICADA".equals(q.getSituacao());
                boolean doProfessorDaTurma = "EM_REVISAO".equals(q.getSituacao())
                        && d.turmasDoProf.getOrDefault(q.getIdAutor(), Set.of()).contains(turmaDoAluno);
                if (!publicada && !doProfessorDaTurma) {
                    continue;
                }
            }
            Atividade a = d.atv.get(q.getIdAtividade());
            if (a == null) {
                continue;
            }
            Conteudo c = d.cont.get(a.getIdConteudo());
            List<Alternativa> alts = alternativas.findByIdQuestaoOrderByIdAlternativaAsc(q.getIdQuestao());
            int correta = 0;
            for (int i = 0; i < alts.size(); i++) {
                if (Boolean.TRUE.equals(alts.get(i).getCorreta())) {
                    correta = i;
                }
            }
            Usuario autor = d.usuario.get(q.getIdAutor());
            out.add(mapa("id", q.getIdQuestao(), "subj", d.cod.get(c.getIdDisciplina()), "idAtividade", a.getIdAtividade(),
                    "ano", c.getAnoEscolar(), "prompt", q.getEnunciado(), "options", alts.stream().map(Alternativa::getTexto).toList(),
                    "answer", correta, "hint", q.getDica(), "author", autor == null ? "Professor" : autor.getNome(),
                    "status", Catalogo.situacaoQuestao(q.getSituacao()), "diff", a.getDificuldade(), "type", q.getTipo()));
        }
        return out;
    }

    // ------------------------------------------------------------------ alunos

    private Map<String, Object> resumoAluno(Dados d, Aluno a) {
        Integer id = a.getIdUsuario();
        int ano = d.turma.get(a.getIdTurma()).getAnoEscolar();
        List<Realizacao> rs = d.realiz.getOrDefault(id, List.of());
        Map<Integer, Integer> melhor = melhorPorAtividade(rs);
        Map<String, Integer> prog = new LinkedHashMap<>();
        for (String codigo : d.anos.keySet()) {
            List<Atividade> l = d.trilha.get(ano + ":" + codigo);
            if (!d.anos.get(codigo).contains(ano) || l == null || l.isEmpty()) {
                continue;
            }
            long feitos = l.stream().filter(x -> melhor.getOrDefault(x.getIdAtividade(), 0) > 0).count();
            prog.put(codigo, (int) Math.round(feitos * 100.0 / l.size()));
        }
        int acertos = (int) Math.round(rs.stream().filter(r -> r.getPercentualAcertos() != null)
                .mapToDouble(r -> r.getPercentualAcertos().doubleValue()).average().orElse(0));
        List<String> dificuldadesAbertas = dificuldadesAbertas(d, id);
        LocalDateTime ultima = rs.stream().map(Realizacao::getDataEnvio).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        int dias = ultima == null ? 99 : (int) ChronoUnit.DAYS.between(ultima.toLocalDate(), d.agora.toLocalDate());
        return mapa("id", str(id), "name", d.usuario.get(id).getNome(), "email", d.usuario.get(id).getEmail(),
                "turma", str(a.getIdTurma()), "ano", ano, "prog", prog, "acc", acertos,
                "diff", dificuldadesAbertas, "strong", fortes(d, rs, dificuldadesAbertas), "en", habilitadas(d, id),
                "days", dias, "min", minutosNaSemana(rs, d.agora), "eq", visual(d, a), "estrelas", d.ganhos.getOrDefault(id, 0L));
    }

    private Map<String, Object> crianca(Dados d, Aluno a) {
        Integer id = a.getIdUsuario();
        Usuario u = d.usuario.get(id);
        List<Realizacao> rs = d.realiz.getOrDefault(id, List.of());
        List<String> possui = new ArrayList<>(List.of("acc:none", "bag:none", "pet:none"));
        for (AlunoItem ai : d.itensAluno.getOrDefault(id, List.of())) {
            ItemLoja i = d.item.get(ai.getIdItem());
            possui.add(Catalogo.CATEGORIA_TELA.get(i.getCategoria()) + ":" + i.getCodigo());
        }
        List<Map<String, Object>> difs = d.difs.getOrDefault(id, List.of()).stream().filter(x -> !Boolean.TRUE.equals(x.getResolvida()))
                .map(x -> {
                    Habilidade h = d.hab.get(x.getIdHabilidade());
                    return mapa("skill", h.getNome(), "subj", d.cod.get(h.getIdDisciplina()), "n", x.getQtdErros(), "resolved", false);
                }).toList();
        List<String> superadas = d.difs.getOrDefault(id, List.of()).stream().filter(x -> Boolean.TRUE.equals(x.getResolvida()))
                .map(x -> d.hab.get(x.getIdHabilidade()).getNome()).distinct().toList();
        List<MensagemTutor> msgs = new ArrayList<>(mensagens.findTop30ByIdAlunoOrderByIdMensagemDesc(id));
        Collections.reverse(msgs);
        Map<String, Integer> estrelas = new HashMap<>();
        melhorPorAtividade(rs).forEach((k, v) -> estrelas.put(str(k), v));
        return mapa("id", str(id), "nome", u.getNome().split(" ")[0], "full", u.getNome(), "turma", str(a.getIdTurma()),
                "ano", d.turma.get(a.getIdTurma()).getAnoEscolar(), "wallet", alunos.saldo(id), "earned", d.ganhos.getOrDefault(id, 0L),
                "eq", visual(d, a), "owned", possui, "diffs", difs, "mastered", superadas,
                "answers", mapa("right", rs.stream().mapToInt(r -> nz(r.getAcertos())).sum(), "wrong", rs.stream().mapToInt(r -> nz(r.getErros())).sum()),
                "min", minutosNaSemana(rs, d.agora), "skills", habilidadesDoAluno(d, rs),
                "persist", rs.stream().anyMatch(r -> nz(r.getErros()) > 0 && nz(r.getEstrelas()) > 0),
                "buys", alunos.compras(id), "stars", estrelas, "en", habilitadas(d, id),
                "chat", msgs.stream().map(m -> mapa("f", "ALUNO".equals(m.getAutor()) ? "k" : "t", "x", m.getTexto())).toList());
    }

    private Map<String, Object> professor(Dados d, Usuario p) {
        Set<String> disc = d.disciplinasDoProf.getOrDefault(p.getIdUsuario(), Set.of());
        String area = disc.isEmpty() ? "Sem turma ainda" : disc.equals(Set.of("ing")) ? "Inglês" : "Polivalente";
        return mapa("id", str(p.getIdUsuario()), "name", p.getNome(), "email", p.getEmail(), "area", area,
                "last", Catalogo.ultimoAcesso(p.getUltimoAcesso(), d.agora),
                "turmas", d.turmasDoProf.getOrDefault(p.getIdUsuario(), Set.of()).stream().map(BootstrapService::str).toList());
    }

    private Map<Integer, Integer> melhorPorAtividade(List<Realizacao> rs) {
        Map<Integer, Integer> m = new HashMap<>();
        for (Realizacao r : rs) {
            if ("TRILHA".equals(r.getModo()) && nz(r.getEstrelas()) > 0) {
                m.merge(r.getIdAtividade(), r.getEstrelas(), Math::max);
            }
        }
        return m;
    }

    private List<String> dificuldadesAbertas(Dados d, Integer idAluno) {
        return d.difs.getOrDefault(idAluno, List.of()).stream().filter(x -> !Boolean.TRUE.equals(x.getResolvida()))
                .map(x -> d.hab.get(x.getIdHabilidade()).getNome()).distinct().toList();
    }

    /** Habilidades em que o aluno tirou 3 estrelas em pelo menos 2 níveis, com média de 90% ou mais. */
    private List<String> fortes(Dados d, List<Realizacao> rs, List<String> dificuldadesAbertas) {
        Map<String, int[]> por = new HashMap<>();
        for (Realizacao r : rs) {
            Atividade a = d.atv.get(r.getIdAtividade());
            if (a == null || a.getIdHabilidade() == null || r.getPercentualAcertos() == null) {
                continue;
            }
            int[] v = por.computeIfAbsent(d.hab.get(a.getIdHabilidade()).getNome(), k -> new int[3]);
            v[0] += nz(r.getEstrelas()) == 3 ? 1 : 0;
            v[1]++;
            v[2] += r.getPercentualAcertos().intValue();
        }
        return por.entrySet().stream()
                .filter(e -> e.getValue()[0] >= 2 && e.getValue()[2] / e.getValue()[1] >= 90 && !dificuldadesAbertas.contains(e.getKey()))
                .sorted((x, y) -> y.getValue()[0] - x.getValue()[0]).limit(2).map(Map.Entry::getKey).toList();
    }

    private Map<String, List<Integer>> habilidadesDoAluno(Dados d, List<Realizacao> rs) {
        Map<String, List<Integer>> m = new LinkedHashMap<>();
        for (Realizacao r : rs) {
            Atividade a = d.atv.get(r.getIdAtividade());
            if (a == null || a.getIdHabilidade() == null) {
                continue;
            }
            String nome = d.hab.get(a.getIdHabilidade()).getNome();
            List<Integer> v = m.computeIfAbsent(nome, k -> new ArrayList<>(List.of(0, 0)));
            v.set(0, v.get(0) + nz(r.getAcertos()));
            v.set(1, v.get(1) + nz(r.getErros()));
        }
        return m;
    }

    private Map<String, Boolean> habilitadas(Dados d, Integer idAluno) {
        Map<String, Boolean> en = new LinkedHashMap<>();
        d.anos.keySet().forEach(c -> en.put(c, true));
        en.putAll(d.desligadas.getOrDefault(idAluno, Map.of()));
        return en;
    }

    private int minutosNaSemana(List<Realizacao> rs, LocalDateTime agora) {
        LocalDateTime desde = agora.minusDays(7);
        return rs.stream().filter(r -> r.getDataEnvio() != null && r.getDataEnvio().isAfter(desde) && r.getDataInicio() != null)
                .mapToInt(r -> (int) Math.max(1, ChronoUnit.MINUTES.between(r.getDataInicio(), r.getDataEnvio()))).sum();
    }

    /** Personagem do aluno no formato das telas: itens equipados e cores. */
    private Map<String, Object> visual(Dados d, Aluno a) {
        Map<String, Object> eq = mapa("hair", "curto", "shirt", "laranja", "acc", "none", "bag", "none", "pet", "none",
                "skin", a.getCorPele() == null ? "#E8B48A" : a.getCorPele(), "hairC", a.getCorCabelo() == null ? "#3B2416" : a.getCorCabelo());
        for (AlunoItem ai : d.itensAluno.getOrDefault(a.getIdUsuario(), List.of())) {
            if (Boolean.TRUE.equals(ai.getEquipado())) {
                ItemLoja i = d.item.get(ai.getIdItem());
                eq.put(Catalogo.CATEGORIA_TELA.get(i.getCategoria()), i.getCodigo());
            }
        }
        return eq;
    }

    // ------------------------------------------------------------------ indicadores

    private List<YearMonth> ultimosMeses(Dados d) {
        YearMonth atual = YearMonth.from(d.agora);
        List<YearMonth> l = new ArrayList<>();
        for (int i = 4; i >= 0; i--) {
            l.add(atual.minusMonths(i));
        }
        return l;
    }

    /** Progresso médio da turma (% dos níveis do ano concluídos) ao fim de cada um dos últimos 5 meses. */
    private List<Integer> serie(Dados d, Integer idTurma) {
        int ano = d.turma.get(idTurma).getAnoEscolar();
        int total = d.anos.entrySet().stream().filter(e -> e.getValue().contains(ano))
                .mapToInt(e -> d.trilha.getOrDefault(ano + ":" + e.getKey(), List.of()).size()).sum();
        List<Aluno> da = d.aluno.values().stream().filter(a -> a.getIdTurma().equals(idTurma)).toList();
        List<Integer> out = new ArrayList<>();
        for (YearMonth m : ultimosMeses(d)) {
            LocalDateTime fim = m.equals(YearMonth.from(d.agora)) ? d.agora : m.atEndOfMonth().atTime(LocalTime.MAX);
            if (total == 0 || da.isEmpty()) {
                out.add(0);
                continue;
            }
            double soma = 0;
            for (Aluno a : da) {
                long feitos = d.realiz.getOrDefault(a.getIdUsuario(), List.of()).stream()
                        .filter(r -> "TRILHA".equals(r.getModo()) && nz(r.getEstrelas()) > 0 && r.getDataEnvio() != null && !r.getDataEnvio().isAfter(fim))
                        .map(Realizacao::getIdAtividade).distinct().count();
                soma += feitos * 100.0 / total;
            }
            out.add((int) Math.round(soma / da.size()));
        }
        return out;
    }

    // ------------------------------------------------------------------ IA: insights

    private void gerarInsightsProfessor(Dados d, Set<Integer> minhas, List<Map<String, Object>> lista) {
        for (Integer tid : minhas) {
            String nomeTurma = d.turma.get(tid).getNome();
            List<Map<String, Object>> al = lista.stream().filter(x -> str(tid).equals(x.get("turma"))).toList();
            if (al.isEmpty()) {
                continue;
            }
            Map.Entry<String, Long> dif = maisFrequente(al, "diff");
            if (dif != null && dif.getValue() >= 3) {
                Integer idHab = ia.idHabilidade(dif.getKey());
                ia.salvar("PROFESSOR", tid, null, idHab, "DIFICULDADE",
                        dif.getValue() + " alunos do " + nomeTurma + " apresentam dificuldade em " + dif.getKey(), IaService.padraoDeErro(dif.getKey()));
                ia.salvar("PROFESSOR", tid, null, idHab, "RECOMENDACAO", "Aplicar reforço de " + dif.getKey() + " no " + nomeTurma + " nesta semana",
                        "Um treino curto de 10 minutos por dia, durante uma semana, costuma resolver. Quem errou já recebe o treino do tutor no mapa.");
            }
            Map.Entry<String, Long> forte = maisFrequente(al, "strong");
            if (forte != null && forte.getValue() >= 3) {
                ia.salvar("PROFESSOR", tid, null, ia.idHabilidade(forte.getKey()), "AVANCO",
                        forte.getValue() + " alunos do " + nomeTurma + " estão avançando rápido em " + forte.getKey(),
                        "Acertam mais de 90% e conseguem 3 estrelas na maioria dos níveis. Vale liberar desafios extras.");
            }
            long inativos = al.stream().filter(x -> (int) x.get("days") > 5).count();
            if (inativos >= 2) {
                ia.salvar("PROFESSOR", tid, null, null, "ATENCAO", inativos + " alunos do " + nomeTurma + " não entram há mais de 5 dias",
                        "Vale um recado para as famílias. Quem volta na mesma semana costuma recuperar o ritmo.");
            }
        }
    }

    private void gerarInsightsEscola(Dados d, List<Map<String, Object>> lista, Map<String, List<Integer>> series) {
        Map<String, Long> principal = new HashMap<>();
        int turmasMaiores = 0;
        for (Turma t : d.turma.values()) {
            if (t.getAnoEscolar() < 4) {
                continue;
            }
            List<Map<String, Object>> al = lista.stream().filter(x -> str(t.getIdTurma()).equals(x.get("turma"))).toList();
            Map.Entry<String, Long> top = maisFrequente(al, "diff");
            if (!al.isEmpty()) {
                turmasMaiores++;
            }
            if (top != null) {
                principal.merge(top.getKey(), 1L, Long::sum);
            }
        }
        Map.Entry<String, Long> geral = principal.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        if (geral != null) {
            ia.salvar("COORDENADOR", null, null, ia.idHabilidade(geral.getKey()), "DIFICULDADE",
                    Catalogo.capitalizar(geral.getKey()) + " é a principal dificuldade em " + geral.getValue() + " de " + turmasMaiores + " turmas do 4º e 5º ano",
                    "O padrão se repete entre professores diferentes, então é um tema da escola, não de uma turma. " + IaService.padraoDeErro(geral.getKey()));
        }
        series.entrySet().stream().filter(e -> e.getValue().size() == 5)
                .max(Comparator.comparingInt(e -> e.getValue().get(4) - e.getValue().get(3)))
                .filter(e -> e.getValue().get(4) - e.getValue().get(3) > 0)
                .ifPresent(e -> ia.salvar("COORDENADOR", null, null, null, "AVANCO",
                        d.turma.get(Integer.valueOf(e.getKey())).getNome() + " avançou " + (e.getValue().get(4) - e.getValue().get(3)) + " pontos no último mês",
                        "É a turma que mais evoluiu no mês. Vale trocar experiências com os outros professores."));
        long inativos = lista.stream().filter(x -> (int) x.get("days") > 5).count();
        if (inativos > 0) {
            ia.salvar("COORDENADOR", null, null, null, "ATENCAO", inativos + " alunos sem atividade há mais de 5 dias",
                    "Vale combinar com os professores um recado para as famílias.");
        }
        long pendentes = questoes.countBySituacao("EM_REVISAO");
        if (pendentes > 0) {
            ia.salvar("COORDENADOR", null, null, null, "RECOMENDACAO", pendentes + " atividades criadas por professores aguardam revisão",
                    "Elas já aparecem para os alunos dos autores. A aprovação libera para a escola toda.");
        }
        Map<String, double[]> media = new HashMap<>();
        for (Map<String, Object> x : lista) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> prog = (Map<String, Integer>) x.get("prog");
            prog.forEach((c, v) -> {
                double[] s = media.computeIfAbsent(c, k -> new double[2]);
                s[0] += v;
                s[1]++;
            });
        }
        media.entrySet().stream().min(Comparator.comparingDouble(e -> e.getValue()[0] / e.getValue()[1])).ifPresent(e -> {
            String nome = d.disc.values().stream().filter(x -> x.getCodigo().equals(e.getKey())).findFirst().map(Disciplina::getNome).orElse(e.getKey());
            ia.salvar("COORDENADOR", null, null, null, "RECOMENDACAO", nome + " tem o menor progresso médio da escola",
                    "Média de " + Math.round(e.getValue()[0] / e.getValue()[1]) + "% dos níveis concluídos. Vale revisar se a trilha está difícil para os anos em que é oferecida.");
        });
    }

    @SuppressWarnings("unchecked")
    private static Map.Entry<String, Long> maisFrequente(List<Map<String, Object>> alunos, String campo) {
        return alunos.stream().flatMap(x -> ((List<String>) x.get(campo)).stream())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
    }

    private static Map<String, Object> insight(InsightIa i) {
        return mapa("id", i.getIdInsight(), "tipo", i.getTipo(), "titulo", i.getTitulo(), "texto", i.getTexto(),
                "situacao", i.getSituacao(), "idAluno", str(i.getIdAluno()), "idTurma", str(i.getIdTurma()), "publico", i.getPublico());
    }

    // ------------------------------------------------------------------ utilidades

    /** Mapa ordenado que aceita valores nulos (Map.of não aceita). */
    public static Map<String, Object> mapa(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    static String str(Integer v) {
        return v == null ? null : String.valueOf(v);
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
