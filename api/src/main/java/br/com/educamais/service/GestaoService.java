package br.com.educamais.service;

import br.com.educamais.model.*;
import br.com.educamais.repository.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Telas da coordenação: professores, turmas, disciplinas da escola e situação dos insights. */
@Service
@RequiredArgsConstructor
public class GestaoService {

    private final UsuarioRepository usuarios;
    private final TurmaRepository turmas;
    private final DisciplinaRepository disciplinas;
    private final DisciplinaAnoRepository disciplinaAnos;
    private final DisciplinaTurmaRepository disciplinaTurmas;
    private final InsightIaRepository insights;

    @Transactional
    public Map<String, Object> convidarProfessor(String nome, String email, String area) {
        if (nome == null || nome.isBlank() || email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe nome e e-mail Google do professor");
        }
        if (usuarios.findByEmail(email.trim().toLowerCase()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe alguém com este e-mail");
        }
        Usuario u = new Usuario();
        u.setNome(nome.trim());
        u.setEmail(email.trim().toLowerCase());
        u.setPerfil("PROFESSOR");
        usuarios.saveAndFlush(u);
        return BootstrapService.mapa("id", String.valueOf(u.getIdUsuario()), "name", u.getNome(), "email", u.getEmail(),
                "area", area == null || area.isBlank() ? "Polivalente" : area, "last", "Convite enviado", "turmas", List.of());
    }

    /** Cria a turma e liga o professor a todas as disciplinas que a escola oferece naquele ano. */
    @Transactional
    public Map<String, Object> criarTurma(String nome, Integer ano, Integer idProfessor) {
        if (nome == null || nome.isBlank() || ano == null || ano < 1 || ano > 9) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome e o ano da turma");
        }
        usuarios.findById(idProfessor).filter(u -> "PROFESSOR".equals(u.getPerfil()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escolha um professor"));
        Turma t = new Turma();
        t.setNome(nome.trim());
        t.setAnoEscolar(ano);
        t.setAnoLetivo(Year.now().getValue());
        turmas.saveAndFlush(t);
        for (DisciplinaAno da : disciplinaAnos.findAll()) {
            if (da.getAnoEscolar().equals(ano)) {
                DisciplinaTurma dt = new DisciplinaTurma();
                dt.setIdTurma(t.getIdTurma());
                dt.setIdDisciplina(da.getIdDisciplina());
                dt.setIdProfessor(idProfessor);
                dt.setDataVinculo(LocalDate.now());
                disciplinaTurmas.save(dt);
            }
        }
        return BootstrapService.mapa("id", String.valueOf(t.getIdTurma()), "name", t.getNome(), "ano", ano,
                "prof", String.valueOf(idProfessor), "n", 0);
    }

    /** Anos escolares em que a escola oferece a disciplina (lista vazia = desativada na escola). */
    @Transactional
    public void definirAnos(String codigo, List<Integer> anos) {
        Disciplina d = disciplinas.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
        disciplinaAnos.deleteByIdDisciplina(d.getIdDisciplina());
        disciplinaAnos.flush();
        for (Integer ano : anos.stream().distinct().filter(a -> a >= 1 && a <= 9).toList()) {
            DisciplinaAno da = new DisciplinaAno();
            da.setIdDisciplina(d.getIdDisciplina());
            da.setAnoEscolar(ano);
            disciplinaAnos.save(da);
        }
    }

    @Transactional
    public void situacaoInsight(Integer id, String situacao) {
        if (!List.of("NOVO", "APLICADO", "DISPENSADO").contains(situacao)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Situação inválida");
        }
        InsightIa i = insights.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insight não encontrado"));
        i.setSituacao(situacao);
        insights.save(i);
    }
}
