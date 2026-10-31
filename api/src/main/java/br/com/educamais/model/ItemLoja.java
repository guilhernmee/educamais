package br.com.educamais.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Item cosmético comprado com estrelas. Tabela: item_loja. */
@Entity
@Table(name = "item_loja")
@Getter
@Setter
@NoArgsConstructor
public class ItemLoja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Integer idItem;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "nome")
    private String nome;

    @Column(name = "custo_estrelas")
    private Integer custoEstrelas = 0;

    @Column(name = "ativo")
    private Boolean ativo = true;

}
