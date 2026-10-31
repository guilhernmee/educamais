package br.com.educamais.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Conteúdo de um ano escolar; no mapa é uma região da ilha. Tabela: conteudo. */
@Entity
@Table(name = "conteudo")
@Getter
@Setter
@NoArgsConstructor
public class Conteudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conteudo")
    private Integer idConteudo;

    @Column(name = "id_disciplina")
    private Integer idDisciplina;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "material")
    private String material;

    @Column(name = "ano_escolar")
    private Integer anoEscolar;

    @Column(name = "ordem")
    private Integer ordem = 1;

    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

}
