package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtividadeRepository extends JpaRepository<Atividade, Integer> {

    List<Atividade> findByIdConteudoOrderByOrdemAsc(Integer idConteudo);

}
