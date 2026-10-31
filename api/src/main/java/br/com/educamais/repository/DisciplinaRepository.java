package br.com.educamais.repository;

import java.util.Optional;
import br.com.educamais.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Integer> {

    Optional<Disciplina> findByCodigo(String codigo);

}
