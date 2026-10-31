package br.com.educamais.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/** Anos escolares em que a escola oferece cada disciplina. Tabela: disciplina_ano. */
@Entity
@Table(name = "disciplina_ano")
@IdClass(DisciplinaAno.Chave.class)
@Getter
@Setter
@NoArgsConstructor
public class DisciplinaAno {

    @Id
    @Column(name = "id_disciplina")
    private Integer idDisciplina;

    @Id
    @Column(name = "ano_escolar")
    private Integer anoEscolar;

    /** Chave composta. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Chave implements Serializable {
        private Integer idDisciplina;
        private Integer anoEscolar;
    }

}
