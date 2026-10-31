package br.com.educamais.repository;

import java.util.Optional;
import br.com.educamais.model.DificuldadeDetectada;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DificuldadeDetectadaRepository extends JpaRepository<DificuldadeDetectada, Integer> {

    Optional<DificuldadeDetectada> findFirstByIdAlunoAndIdHabilidadeAndResolvidaFalse(Integer idAluno, Integer idHabilidade);

}
