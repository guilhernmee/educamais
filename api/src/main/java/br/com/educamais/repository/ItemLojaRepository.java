package br.com.educamais.repository;

import java.util.Optional;
import br.com.educamais.model.ItemLoja;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemLojaRepository extends JpaRepository<ItemLoja, Integer> {

    Optional<ItemLoja> findByCategoriaAndCodigo(String categoria, String codigo);

}
