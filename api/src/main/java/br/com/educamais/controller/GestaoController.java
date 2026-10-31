package br.com.educamais.controller;

import br.com.educamais.service.BootstrapService;
import br.com.educamais.service.GestaoService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Gestão da escola (coordenação) e situação dos insights da IA. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GestaoController {

    private final GestaoService gestao;

    public record ProfessorDTO(String nome, String email, String area) {
    }

    public record TurmaDTO(String nome, Integer ano, Integer idProfessor) {
    }

    public record AnosDTO(List<Integer> anos) {
    }

    public record SituacaoDTO(String situacao) {
    }

    @PostMapping("/professores")
    public Map<String, Object> convidar(@RequestBody ProfessorDTO dto) {
        return gestao.convidarProfessor(dto.nome(), dto.email(), dto.area());
    }

    @PostMapping("/turmas")
    public Map<String, Object> criarTurma(@RequestBody TurmaDTO dto) {
        return gestao.criarTurma(dto.nome(), dto.ano(), dto.idProfessor());
    }

    @PutMapping("/disciplinas/{codigo}/anos")
    public Map<String, Object> anos(@PathVariable String codigo, @RequestBody AnosDTO dto) {
        gestao.definirAnos(codigo, dto.anos() == null ? List.of() : dto.anos());
        return BootstrapService.mapa("ok", true);
    }

    /** Aceita PUT também: o cliente HTTP nativo do Android não envia PATCH. */
    @RequestMapping(value = "/insights/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public Map<String, Object> insight(@PathVariable Integer id, @RequestBody SituacaoDTO dto) {
        gestao.situacaoInsight(id, dto.situacao());
        return BootstrapService.mapa("ok", true);
    }
}
