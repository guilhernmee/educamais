package br.com.educamais.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mensagem trocada entre o aluno e o tutor (Tico). Tabela: mensagem_tutor. */
@Entity
@Table(name = "mensagem_tutor")
@Getter
@Setter
@NoArgsConstructor
public class MensagemTutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensagem")
    private Integer idMensagem;

    @Column(name = "id_aluno")
    private Integer idAluno;

    @Column(name = "autor")
    private String autor;

    @Column(name = "texto")
    private String texto;

    @Column(name = "id_questao")
    private Integer idQuestao;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio = LocalDateTime.now();

}
