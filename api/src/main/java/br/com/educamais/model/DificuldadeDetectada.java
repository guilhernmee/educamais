package br.com.educamais.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dificuldade que a IA detectou em um aluno. Tabela: dificuldade_detectada. */
@Entity
@Table(name = "dificuldade_detectada")
@Getter
@Setter
@NoArgsConstructor
public class DificuldadeDetectada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dificuldade")
    private Integer idDificuldade;

    @Column(name = "id_aluno")
    private Integer idAluno;

    @Column(name = "id_habilidade")
    private Integer idHabilidade;

    @Column(name = "qtd_erros")
    private Integer qtdErros = 1;

    @Column(name = "data_deteccao")
    private LocalDateTime dataDeteccao = LocalDateTime.now();

    @Column(name = "resolvida")
    private Boolean resolvida = false;

    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;

}
