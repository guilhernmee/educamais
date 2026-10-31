package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.Questao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestaoRepository extends JpaRepository<Questao, Integer> {

    List<Questao> findByIdAutorIsNotNull();

    List<Questao> findByIdAtividade(Integer idAtividade);

    long countBySituacao(String situacao);

}
