package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.DisciplinaTurma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaTurmaRepository extends JpaRepository<DisciplinaTurma, Integer> {

    List<DisciplinaTurma> findByIdProfessor(Integer idProfessor);

}
