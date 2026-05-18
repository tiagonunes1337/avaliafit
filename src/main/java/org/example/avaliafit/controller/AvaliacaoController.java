package org.example.avaliafit.controller;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.AvaliacaoRequestDTO;
import org.example.avaliafit.dto.AvaliacaoResponseDTO;
import org.example.avaliafit.service.AvaliacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permite o seu front-end acessar
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    // Registra nova avaliação (Somente equipe pode criar)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<AvaliacaoResponseDTO> registrar(@RequestBody AvaliacaoRequestDTO dto) {
        return ResponseEntity.ok(avaliacaoService.registrar(dto));
    }

    @GetMapping("/paciente/{idPaciente}/ultima")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE', 'ROLE_FUNCIONARIO', 'ROLE_PACIENTE') or hasAnyAuthority('ADMIN', 'GERENTE', 'FUNCIONARIO', 'PACIENTE')")
    public ResponseEntity<AvaliacaoResponseDTO> buscarUltima(@PathVariable Integer idPaciente) {
        return ResponseEntity.ok(avaliacaoService.buscarUltimaAvaliacao(idPaciente));
    }

    // Lista todas as avaliações de um paciente (Histórico)
    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO', 'PACIENTE')")
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarPorPaciente(@PathVariable Integer idPaciente) {
        return ResponseEntity.ok(avaliacaoService.listarPorPaciente(idPaciente));
    }
}