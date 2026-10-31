package br.com.educamais.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Atividade; no mapa é um nível da trilha. Tabela: atividade. */
@Entity
@Table(name = "atividade")
@Getter
@Setter
@NoArgsConstructor
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atividade")
    private Integer idAtividade;

    @Column(name = "id_conteudo")
    private Integer idConteudo;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "ordem")
    private Integer ordem = 1;

    @Column(name = "dificuldade")
    private Integer dificuldade = 1;

    @Column(name = "tipo_minijogo")
    private String tipoMinijogo;

    @Column(name = "id_habilidade")
    private Integer idHabilidade;

    @Column(name = "id_autor")
    private Integer idAutor;

    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

}
