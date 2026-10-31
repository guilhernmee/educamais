package br.com.educamais.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Disciplina, mostrada como uma ilha no mapa. Tabela: disciplina. */
@Entity
@Table(name = "disciplina")
@Getter
@Setter
@NoArgsConstructor
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disciplina")
    private Integer idDisciplina;

    @Column(name = "nome")
    private String nome;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "nome_ilha")
    private String nomeIlha;

    @Column(name = "cor")
    private String cor;

}
