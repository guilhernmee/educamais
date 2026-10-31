package br.com.educamais.repository;

import java.util.List;
import br.com.educamais.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface AlunoRepository extends JpaRepository<Aluno, Integer> {

    List<Aluno> findByIdTurma(Integer idTurma);

    @Query(value = "SELECT saldo_estrelas FROM aluno WHERE id_usuario = ?1", nativeQuery = true)
    Integer saldo(Integer idAluno);

    @Query(value = "SELECT id_aluno, SUM(quantidade) FROM movimento_estrelas WHERE tipo = 'GANHO' GROUP BY id_aluno", nativeQuery = true)
    List<Object[]> ganhosPorAluno();

    @Query(value = "SELECT COUNT(*) FROM movimento_estrelas WHERE tipo = 'COMPRA' AND id_aluno = ?1", nativeQuery = true)
    Integer compras(Integer idAluno);

    /** Marca como equipado só o item escolhido em cada categoria. */
    @Modifying
    @Query(value = "UPDATE aluno_item ai JOIN item_loja i ON i.id_item = ai.id_item SET ai.equipado = (i.codigo = CASE i.categoria "
            + "WHEN 'CABELO' THEN :cabelo WHEN 'ROUPA' THEN :roupa WHEN 'ACESSORIO' THEN :acessorio WHEN 'MOCHILA' THEN :mochila ELSE :pet END) "
            + "WHERE ai.id_aluno = :aluno", nativeQuery = true)
    int equipar(@Param("aluno") Integer aluno, @Param("cabelo") String cabelo, @Param("roupa") String roupa,
                @Param("acessorio") String acessorio, @Param("mochila") String mochila, @Param("pet") String pet);

    @Modifying
    @Query(value = "UPDATE aluno SET cor_pele = :pele, cor_cabelo = :cabelo WHERE id_usuario = :aluno", nativeQuery = true)
    int salvarCores(@Param("aluno") Integer aluno, @Param("pele") String pele, @Param("cabelo") String cabelo);

    @Modifying
    @Query(value = "INSERT INTO aluno_item (id_aluno, id_item, equipado) SELECT :aluno, id_item, codigo IN ('curto', 'laranja') FROM item_loja WHERE custo_estrelas = 0", nativeQuery = true)
    int darItensIniciais(@Param("aluno") Integer aluno);

}
