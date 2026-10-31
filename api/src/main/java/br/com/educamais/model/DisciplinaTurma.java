package br.com.educamais.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Professor que ensina uma disciplina em uma turma. Tabela: disciplina_turma. */
@Entity
@Table(name = "disciplina_turma")
@Getter
@Setter
@NoArgsConstructor
public class DisciplinaTurma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disciplina_turma")
    private Integer idDisciplinaTurma;

    @Column(name = "id_turma")
    private Integer idTurma;

    @Column(name = "id_disciplina")
    private Integer idDisciplina;

    @Column(name = "id_professor")
    private Integer idProfessor;

    @Column(name = "data_vinculo")
    private LocalDate dataVinculo;

}
