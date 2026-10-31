package br.com.educamais.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/** Item que o aluno possui e se está usando no personagem. Tabela: aluno_item. */
@Entity
@Table(name = "aluno_item")
@IdClass(AlunoItem.Chave.class)
@Getter
@Setter
@NoArgsConstructor
public class AlunoItem {

    @Id
    @Column(name = "id_aluno")
    private Integer idAluno;

    @Id
    @Column(name = "id_item")
    private Integer idItem;

    @Column(name = "data_aquisicao", insertable = false, updatable = false)
    private LocalDateTime dataAquisicao;

    @Column(name = "equipado")
    private Boolean equipado = false;

    /** Chave composta. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Chave implements Serializable {
        private Integer idAluno;
        private Integer idItem;
    }

}
