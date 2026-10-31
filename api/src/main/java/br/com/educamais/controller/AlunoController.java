package br.com.educamais.controller;

import br.com.educamais.service.AlunoService;
import br.com.educamais.service.BootstrapService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Ações do aluno no jogo e ações do professor sobre um aluno. */
@RestController
@RequestMapping("/api/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunos;
    private final BootstrapService bootstrap;

    public record NovoAlunoDTO(String nome, String email, Integer idTurma) {
    }

    public record DificuldadeDTO(String disciplina, String habilidade, Integer erros) {
    }

    public record CompraDTO(String parte, String codigo) {
    }

    public record MensagemDTO(String autor, String texto) {
    }

    public record DisciplinaDTO(Boolean habilitada, Integer idProfessor) {
    }

    @PostMapping
    public Map<String, Object> cadastrar(@RequestBody NovoAlunoDTO dto) {
        return bootstrap.resumo(alunos.cadastrar(dto.nome(), dto.email(), dto.idTurma()));
    }

    @GetMapping("/{id}/detalhe")
    public Map<String, Object> detalhe(@PathVariable Integer id) {
        return bootstrap.detalhe(id);
    }

    @PostMapping("/{id}/partidas")
    public Map<String, Object> partida(@PathVariable Integer id, @RequestBody AlunoService.PartidaDTO dto) {
        return alunos.registrarPartida(id, dto);
    }

    @PostMapping("/{id}/dificuldades")
    public Map<String, Object> dificuldade(@PathVariable Integer id, @RequestBody DificuldadeDTO dto) {
        return alunos.registrarDificuldade(id, dto.disciplina(), dto.habilidade(), dto.erros());
    }

    @PostMapping("/{id}/dificuldades/resolver")
    public Map<String, Object> resolver(@PathVariable Integer id, @RequestBody DificuldadeDTO dto) {
        return alunos.resolverDificuldade(id, dto.habilidade());
    }

    @PostMapping("/{id}/compras")
    public Map<String, Object> comprar(@PathVariable Integer id, @RequestBody CompraDTO dto) {
        return alunos.comprar(id, dto.parte(), dto.codigo());
    }

    @PutMapping("/{id}/personagem")
    public Map<String, Object> personagem(@PathVariable Integer id, @RequestBody Map<String, String> eq) {
        return alunos.salvarPersonagem(id, eq);
    }

    @PostMapping("/{id}/mensagens")
    public Map<String, Object> mensagem(@PathVariable Integer id, @RequestBody MensagemDTO dto) {
        return alunos.mensagem(id, dto.autor(), dto.texto() == null ? "" : dto.texto());
    }

    @PutMapping("/{id}/disciplinas/{codigo}")
    public Map<String, Object> disciplina(@PathVariable Integer id, @PathVariable String codigo, @RequestBody DisciplinaDTO dto) {
        return alunos.definirDisciplina(id, codigo, dto.habilitada(), dto.idProfessor());
    }
}
