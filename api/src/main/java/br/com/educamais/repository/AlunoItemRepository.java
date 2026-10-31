package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.AlunoItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoItemRepository extends JpaRepository<AlunoItem, AlunoItem.Chave> {

    List<AlunoItem> findByIdAluno(Integer idAluno);

}
