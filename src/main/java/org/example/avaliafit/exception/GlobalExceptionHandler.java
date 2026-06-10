package org.example.avaliafit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// ============================================================
//  O QUE É ESSA CLASSE?
//
//  Um @RestControllerAdvice é um interceptador global de erros.
//  Em vez de colocar try/catch em cada controller, você coloca
//  aqui uma vez — e ele funciona para TODOS os controllers.
//
//  FLUXO:
//  [Requisição com @Valid] → Spring valida o DTO → falha
//       ↓
//  Spring procura um @ExceptionHandler que trate esse erro
//       ↓
//  Encontra esse arquivo → executa → retorna resposta certinha
// ============================================================
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================================
    //  HANDLER: erros de validação do @Valid
    //
    //  Quando um campo do DTO falha na validação
    //  (ex: senha menor que 6 chars, email inválido, campo vazio),
    //  o Spring lança MethodArgumentNotValidException.
    //
    //  Esse método pega o PRIMEIRO erro encontrado e devolve
    //  a mensagem que você escreveu no DTO — por exemplo:
    //    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    //                             ↑ essa mensagem aparece no alert
    // ============================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // pega a mensagem do primeiro campo que falhou
        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        return ResponseEntity
                .badRequest()
                .body(Map.of("mensagem", mensagem));
    }
    
}