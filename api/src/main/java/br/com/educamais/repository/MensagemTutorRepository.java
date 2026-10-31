package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.MensagemTutor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemTutorRepository extends JpaRepository<MensagemTutor, Integer> {

    List<MensagemTutor> findTop30ByIdAlunoOrderByIdMensagemDesc(Integer idAluno);

}
