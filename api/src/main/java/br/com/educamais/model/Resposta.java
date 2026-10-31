package br.com.educamais.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Resposta final do aluno a uma questão do banco. Tabela: resposta. */
@Entity
@Table(name = "resposta")
@Getter
@Setter
@NoArgsConstructor
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resposta")
    private Integer idResposta;

    @Column(name = "id_realizacao")
    private Integer idRealizacao;

    @Column(name = "id_questao")
    private Integer idQuestao;

    @Column(name = "id_alternativa")
    private Integer idAlternativa;

    @Column(name = "correta")
    private Boolean correta;

    @Column(name = "tentativas")
    private Integer tentativas = 1;

    @Column(name = "tempo_resposta_seg")
    private Integer tempoRespostaSeg;

    @Column(name = "usou_dica")
    private Boolean usouDica = false;

    @Column(name = "data_resposta", insertable = false, updatable = false)
    private LocalDateTime dataResposta;

}
