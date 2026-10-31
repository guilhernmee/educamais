package br.com.educamais.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Habilidade que a IA acompanha (ex.: subtração). Tabela: habilidade. */
@Entity
@Table(name = "habilidade")
@Getter
@Setter
@NoArgsConstructor
public class Habilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habilidade")
    private Integer idHabilidade;

    @Column(name = "id_disciplina")
    private Integer idDisciplina;

    @Column(name = "nome")
    private String nome;

}
