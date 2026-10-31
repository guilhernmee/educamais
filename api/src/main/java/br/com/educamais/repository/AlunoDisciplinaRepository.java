package br.com.educamais.repository;

import br.com.educamais.model.AlunoDisciplina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoDisciplinaRepository extends JpaRepository<AlunoDisciplina, AlunoDisciplina.Chave> {
}
