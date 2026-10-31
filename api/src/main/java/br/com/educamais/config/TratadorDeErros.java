package br.com.educamais.config;

import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Devolve todo erro como {"erro": "mensagem"}, que é o que o app mostra para o usuário. */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("erro", String.valueOf(e.getReason())));
    }

    /** Erros do banco, inclusive os SIGNAL da procedure de compra ("Estrelas insuficientes"). */
    @ExceptionHandler({DataAccessException.class, jakarta.persistence.PersistenceException.class})
    public ResponseEntity<Map<String, String>> banco(Exception e) {
        Throwable raiz = e;
        while (raiz.getCause() != null && raiz.getCause() != raiz) {
            raiz = raiz.getCause();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", String.valueOf(raiz.getMessage())));
    }
}
