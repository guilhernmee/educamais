package br.com.educamais.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Desafio de uma atividade. As questões dos professores passam por revisão. Tabela: questao. */
@Entity
@Table(name = "questao")
@Getter
@Setter
@NoArgsConstructor
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_questao")
    private Integer idQuestao;

    @Column(name = "id_atividade")
    private Integer idAtividade;

    @Column(name = "enunciado")
    private String enunciado;

    @Column(name = "ordem")
    private Integer ordem;

    @Column(name = "tipo")
    private String tipo = "MULTIPLA_ESCOLHA";

    @Column(name = "dica")
    private String dica;

    @Column(name = "midia")
    private String midia;

    @Column(name = "id_habilidade")
    private Integer idHabilidade;

    @Column(name = "id_autor")
    private Integer idAutor;

    @Column(name = "situacao")
    private String situacao = "PUBLICADA";

}
