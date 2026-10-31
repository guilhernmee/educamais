package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.DisciplinaAno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaAnoRepository extends JpaRepository<DisciplinaAno, DisciplinaAno.Chave> {

    List<DisciplinaAno> findByIdDisciplina(Integer idDisciplina);

    void deleteByIdDisciplina(Integer idDisciplina);

}
