package br.com.educamais.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dados do usuário com perfil de aluno. O saldo de estrelas é mantido por gatilho no banco, por isso a API só lê. Tabela: aluno. */
@Entity
@Table(name = "aluno")
@Getter
@Setter
@NoArgsConstructor
public class Aluno {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_turma")
    private Integer idTurma;

    @Column(name = "saldo_estrelas", insertable = false, updatable = false)
    private Integer saldoEstrelas;

    @Column(name = "cor_pele")
    private String corPele;

    @Column(name = "cor_cabelo")
    private String corCabelo;

}
