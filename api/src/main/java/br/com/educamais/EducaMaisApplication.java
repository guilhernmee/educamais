package br.com.educamais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** API do Educa+. Ao subir, as telas também ficam em http://localhost:8080 para testar no navegador. */
@SpringBootApplication
public class EducaMaisApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducaMaisApplication.class, args);
    }
}
