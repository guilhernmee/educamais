package br.com.educamais.controller;

import br.com.educamais.service.BootstrapService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Carga inicial das telas, de acordo com o perfil de quem entrou. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BootstrapController {

    private final BootstrapService bootstrap;

    @GetMapping("/bootstrap/{idUsuario}")
    public Map<String, Object> carregar(@PathVariable Integer idUsuario) {
        return bootstrap.carregar(idUsuario);
    }
}
