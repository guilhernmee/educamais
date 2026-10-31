package br.com.educamais.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Turma de um ano escolar. Tabela: turma. */
@Entity
@Table(name = "turma")
@Getter
@Setter
@NoArgsConstructor
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turma")
    private Integer idTurma;

    @Column(name = "nome")
    private String nome;

    @Column(name = "ano_escolar")
    private Integer anoEscolar;

    @Column(name = "ano_letivo")
    private Integer anoLetivo;

    @Column(name = "ativa")
    private Boolean ativa = true;

}
