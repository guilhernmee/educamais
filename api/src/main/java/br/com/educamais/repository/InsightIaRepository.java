package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.InsightIa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsightIaRepository extends JpaRepository<InsightIa, Integer> {

    List<InsightIa> findByPublicoOrderByIdInsightDesc(String publico);

}
