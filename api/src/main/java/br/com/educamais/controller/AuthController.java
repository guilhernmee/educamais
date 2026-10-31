package br.com.educamais.controller;

import br.com.educamais.model.Usuario;
import br.com.educamais.repository.UsuarioRepository;
import br.com.educamais.service.BootstrapService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Entrada no app. Por enquanto com contas de teste; no login Google de verdade, o app manda o
 * e-mail (e o google_sub) confirmados pelo Google e o resto do fluxo continua igual.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final List<String[]> CONTAS_TESTE = List.of(
            new String[]{"lia.martins@aluno.escolahorizonte.edu.br", "Aluna · 4º A"},
            new String[]{"joao.alves@aluno.escolahorizonte.edu.br", "Aluno · 4º A (sem História)"},
            new String[]{"carla.nogueira@escolahorizonte.edu.br", "Professora · 3º A, 4º A, 4º B"},
            new String[]{"paulo.andrade@escolahorizonte.edu.br", "Coordenador"});

    private final UsuarioRepository usuarios;

    public record LoginDTO(String email) {
    }

    @GetMapping("/contas-teste")
    public List<Map<String, Object>> contasTeste() {
        return CONTAS_TESTE.stream().map(c -> usuarios.findByEmail(c[0]).map(u -> BootstrapService.mapa(
                "id", String.valueOf(u.getIdUsuario()), "nome", u.getNome(), "email", u.getEmail(),
                "perfil", u.getPerfil(), "detalhe", c[1])).orElse(null)).filter(m -> m != null).toList();
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginDTO dto) {
        String email = dto.email() == null ? "" : dto.email().trim().toLowerCase();
        Usuario u = usuarios.findByEmail(email).filter(x -> Boolean.TRUE.equals(x.getAtivo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não cadastrada no Educa+"));
        u.setUltimoAcesso(LocalDateTime.now());
        usuarios.save(u);
        return BootstrapService.mapa("id", String.valueOf(u.getIdUsuario()), "nome", u.getNome(), "email", u.getEmail(), "perfil", u.getPerfil());
    }
}
