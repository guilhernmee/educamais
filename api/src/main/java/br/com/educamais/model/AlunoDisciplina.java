package br.com.educamais.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/** Disciplina ligada ou desligada para um aluno. Tabela: aluno_disciplina. */
@Entity
@Table(name = "aluno_disciplina")
@IdClass(AlunoDisciplina.Chave.class)
@Getter
@Setter
@NoArgsConstructor
public class AlunoDisciplina {

    @Id
    @Column(name = "id_aluno")
    private Integer idAluno;

    @Id
    @Column(name = "id_disciplina")
    private Integer idDisciplina;

    @Column(name = "habilitada")
    private Boolean habilitada = true;

    @Column(name = "id_professor_alteracao")
    private Integer idProfessorAlteracao;

    @Column(name = "data_alteracao")
    private LocalDateTime dataAlteracao = LocalDateTime.now();

    /** Chave composta. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Chave implements Serializable {
        private Integer idAluno;
        private Integer idDisciplina;
    }

}
