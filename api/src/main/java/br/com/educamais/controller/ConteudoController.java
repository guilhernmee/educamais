package br.com.educamais.controller;

import br.com.educamais.service.BootstrapService;
import br.com.educamais.service.ConteudoService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Trilhas das ilhas e questões criadas por professores. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConteudoController {

    private final ConteudoService conteudos;

    public record OrdemDTO(List<Integer> ids) {
    }

    public record NivelDTO(Integer ano, String disciplina, String titulo, Integer dificuldade, String tipoMinijogo, Integer idAutor) {
    }

    public record SituacaoDTO(String situacao) {
    }

    @PutMapping("/trilhas/{ano}/{codigo}/ordem")
    public Map<String, Object> reordenar(@PathVariable int ano, @PathVariable String codigo, @RequestBody OrdemDTO dto) {
        conteudos.reordenar(ano, codigo, dto.ids());
        return BootstrapService.mapa("ok", true);
    }

    @PostMapping("/niveis")
    public Map<String, Object> criarNivel(@RequestBody NivelDTO dto) {
        return conteudos.criarNivel(dto.ano(), dto.disciplina(), dto.titulo(), dto.dificuldade(), dto.tipoMinijogo(), dto.idAutor());
    }

    @PutMapping("/niveis/{id}")
    public Map<String, Object> editarNivel(@PathVariable Integer id, @RequestBody NivelDTO dto) {
        conteudos.editarNivel(id, dto.titulo(), dto.dificuldade());
        return BootstrapService.mapa("ok", true);
    }

    @PostMapping("/questoes")
    public Map<String, Object> criarQuestao(@RequestBody ConteudoService.QuestaoDTO dto) {
        return conteudos.criarQuestao(dto);
    }

    /** Aceita PUT também: o cliente HTTP nativo do Android não envia PATCH. */
    @RequestMapping(value = "/questoes/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public Map<String, Object> situacao(@PathVariable Integer id, @RequestBody SituacaoDTO dto) {
        conteudos.situacaoQuestao(id, dto.situacao());
        return BootstrapService.mapa("ok", true);
    }
}
