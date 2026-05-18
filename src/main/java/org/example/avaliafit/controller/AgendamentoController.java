package org.example.avaliafit.controller;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.AgendamentoRequestDTO;
import org.example.avaliafit.dto.AgendamentoResponseDTO;
import org.example.avaliafit.service.AgendamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permite o acesso do HTML
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    // Criar um novo agendamento (Paciente e Funcionários podem)
    @PostMapping
    @PreAuthorize("hasAnyRole('PACIENTE', 'FUNCIONARIO', 'ADMIN', 'GERENTE')")
    public ResponseEntity<?> agendar(@RequestBody AgendamentoRequestDTO dto) {
        try {
            return ResponseEntity.ok(agendamentoService.agendar(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }

    // Listar agendamentos de um paciente específico
    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasAnyRole('PACIENTE', 'FUNCIONARIO', 'ADMIN', 'GERENTE')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarPorPaciente(@PathVariable Integer idPaciente) {
        return ResponseEntity.ok(agendamentoService.listarPorPaciente(idPaciente));
    }

    @GetMapping("/para-avaliacao")
    @PreAuthorize("hasAnyRole(\'ADMIN\', \'GERENTE\', \'FUNCIONARIO\')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarAgendamentosParaAvaliacao() {
        return ResponseEntity.ok(agendamentoService.listarAgendamentosParaAvaliacao());
    }
}