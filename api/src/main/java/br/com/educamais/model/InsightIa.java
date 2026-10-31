package br.com.educamais.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Insight gerado pela IA para professores (por turma) ou para a coordenação. Tabela: insight_ia. */
@Entity
@Table(name = "insight_ia")
@Getter
@Setter
@NoArgsConstructor
public class InsightIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insight")
    private Integer idInsight;

    @Column(name = "publico")
    private String publico;

    @Column(name = "id_turma")
    private Integer idTurma;

    @Column(name = "id_aluno")
    private Integer idAluno;

    @Column(name = "id_habilidade")
    private Integer idHabilidade;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "texto")
    private String texto;

    @Column(name = "situacao")
    private String situacao = "NOVO";

    @Column(name = "data_geracao")
    private LocalDateTime dataGeracao = LocalDateTime.now();

}
