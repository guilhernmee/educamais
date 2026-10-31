package br.com.educamais.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Opção de resposta de uma questão. Tabela: alternativa. */
@Entity
@Table(name = "alternativa")
@Getter
@Setter
@NoArgsConstructor
public class Alternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alternativa")
    private Integer idAlternativa;

    @Column(name = "id_questao")
    private Integer idQuestao;

    @Column(name = "texto")
    private String texto;

    @Column(name = "correta")
    private Boolean correta = false;

    @Column(name = "grupo")
    private String grupo;

    @Column(name = "posicao")
    private Integer posicao;

}
