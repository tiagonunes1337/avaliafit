package org.example.avaliafit.controller;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.HorarioDisponivelRequestDTO;
import org.example.avaliafit.dto.HorarioDisponivelResponseDTO;
import org.example.avaliafit.service.HorarioDisponivelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/horarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HorarioDisponivelController {

    private final HorarioDisponivelService horarioDisponivelService;

    @GetMapping("/disponiveis")
    public ResponseEntity<List<HorarioDisponivelResponseDTO>> listarDisponiveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(horarioDisponivelService.listarDisponiveisPorData(data));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<?> criarHorario(@RequestBody HorarioDisponivelRequestDTO dto) {
        try {
            HorarioDisponivelResponseDTO response = horarioDisponivelService.criarHorario(dto);
            return ResponseEntity.ok().body(Map.of("mensagem", "Horário criado com sucesso!", "id", response.getIdHorario()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<Void> deletarHorario(@PathVariable Integer id) {
        horarioDisponivelService.deletarHorario(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<?> atualizarHorario(@PathVariable Integer id, @RequestBody HorarioDisponivelRequestDTO dto) {
        try {
            return ResponseEntity.ok(horarioDisponivelService.atualizarHorario(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }
}