package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlternativaRepository extends JpaRepository<Alternativa, Integer> {

    List<Alternativa> findByIdQuestaoOrderByIdAlternativaAsc(Integer idQuestao);

}
