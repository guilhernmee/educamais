package br.com.educamais.repository;

import java.util.Optional;
import java.util.List;
import br.com.educamais.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByPerfil(String perfil);

}
