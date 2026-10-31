package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.Conteudo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConteudoRepository extends JpaRepository<Conteudo, Integer> {

    List<Conteudo> findByIdDisciplinaAndAnoEscolarOrderByOrdemAsc(Integer idDisciplina, Integer anoEscolar);

}
