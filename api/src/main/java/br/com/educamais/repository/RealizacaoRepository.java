package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.Realizacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealizacaoRepository extends JpaRepository<Realizacao, Integer> {

    List<Realizacao> findBySituacao(String situacao);

}
