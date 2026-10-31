package br.com.educamais.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/** Traduções entre os valores do banco e os códigos usados pelas telas. */
public final class Catalogo {

    private Catalogo() {
    }

    /** Categoria da loja no banco -> parte do personagem nas telas. */
    public static final Map<String, String> CATEGORIA_TELA = Map.of(
            "CABELO", "hair", "ROUPA", "shirt", "ACESSORIO", "acc", "MOCHILA", "bag", "PET", "pet");

    /** Parte do personagem nas telas -> categoria da loja no banco. */
    public static final Map<String, String> CATEGORIA_BANCO = Map.of(
            "hair", "CABELO", "shirt", "ROUPA", "acc", "ACESSORIO", "bag", "MOCHILA", "pet", "PET");

    /** Tipo de minijogo de Matemática -> habilidade acompanhada pela IA. */
    public static final Map<String, String> HABILIDADE_DO_MINIJOGO = Map.ofEntries(
            Map.entry("contar", "contagem"), Map.entry("soma", "adição"), Map.entry("comparar", "comparação"),
            Map.entry("formas", "formas"), Map.entry("sequencia", "sequências"), Map.entry("problema", "problemas"),
            Map.entry("soma2", "adição com dezenas"), Map.entry("subtracao", "subtração"),
            Map.entry("multi", "multiplicação"), Map.entry("fracao", "frações"));

    public static String situacaoQuestao(String situacao) {
        return switch (situacao) {
            case "EM_REVISAO" -> "Em revisão";
            case "RECUSADA" -> "Recusada";
            default -> "Publicado";
        };
    }

    public static String ultimoAcesso(LocalDateTime quando, LocalDateTime agora) {
        if (quando == null) {
            return "Convite enviado";
        }
        long dias = ChronoUnit.DAYS.between(quando.toLocalDate(), agora.toLocalDate());
        return dias <= 0 ? "Ativo hoje" : dias == 1 ? "Ontem" : "Há " + dias + " dias";
    }

    public static String capitalizar(String s) {
        return s == null || s.isEmpty() ? "" : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
