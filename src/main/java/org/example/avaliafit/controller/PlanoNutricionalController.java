package org.example.avaliafit.controller;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.PlanoNutricionalRequestDTO;
import org.example.avaliafit.dto.PlanoNutricionalResponseDTO;
import org.example.avaliafit.service.PlanoNutricionalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/planos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlanoNutricionalController {

    private final PlanoNutricionalService planoNutricionalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<?> criarPlano(@RequestBody PlanoNutricionalRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(planoNutricionalService.criarPlano(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody PlanoNutricionalRequestDTO dto) {
        try {
            return ResponseEntity.ok(planoNutricionalService.atualizar(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<?> deletar(@PathVariable Integer id) {
        try {
            planoNutricionalService.deletar(id);
            return ResponseEntity.ok(Map.of("mensagem", "Plano removido com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }

    // Plano ativo do paciente — usado pelo dashboard do inicial.html
    @GetMapping("/paciente/{idUsuario}/ativo")
    @PreAuthorize("hasAnyRole('PACIENTE', 'FUNCIONARIO', 'GERENTE', 'ADMIN')")
    public ResponseEntity<?> buscarPlanoAtivo(@PathVariable Integer idUsuario) {
        try {
            return ResponseEntity.ok(planoNutricionalService.buscarPlanoAtivo(idUsuario));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("mensagem", e.getMessage()));
        }
    }

    // Histórico de todos os planos do paciente
    @GetMapping("/paciente/{idUsuario}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO', 'PACIENTE')")
    public ResponseEntity<List<PlanoNutricionalResponseDTO>> listarPorPaciente(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(planoNutricionalService.listarPorPaciente(idUsuario));
    }
}