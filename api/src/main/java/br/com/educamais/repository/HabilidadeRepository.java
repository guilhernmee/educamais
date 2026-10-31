package br.com.educamais.repository;

import java.util.Optional;
import br.com.educamais.model.Habilidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabilidadeRepository extends JpaRepository<Habilidade, Integer> {

    Optional<Habilidade> findFirstByIdDisciplinaAndNome(Integer idDisciplina, String nome);

    Optional<Habilidade> findFirstByNome(String nome);

}
