package br.com.educamais.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Uma partida de um nível. Ao gravar como ENVIADA, o gatilho do banco credita as estrelas. Tabela: realizacao_atividade. */
@Entity
@Table(name = "realizacao_atividade")
@Getter
@Setter
@NoArgsConstructor
public class Realizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_realizacao")
    private Integer idRealizacao;

    @Column(name = "id_aluno")
    private Integer idAluno;

    @Column(name = "id_atividade")
    private Integer idAtividade;

    @Column(name = "id_atribuicao")
    private Integer idAtribuicao;

    @Column(name = "modo")
    private String modo = "TRILHA";

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "situacao")
    private String situacao = "EM_ANDAMENTO";

    @Column(name = "percentual_acertos")
    private BigDecimal percentualAcertos;

    @Column(name = "acertos")
    private Integer acertos = 0;

    @Column(name = "estrelas")
    private Integer estrelas;

    @Column(name = "erros")
    private Integer erros = 0;

    @Column(name = "dicas_usadas")
    private Integer dicasUsadas = 0;

}
