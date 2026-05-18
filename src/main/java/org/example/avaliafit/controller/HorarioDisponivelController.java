package org.example.avaliafit.controller;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.HorarioDisponivelRequestDTO;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.HorarioDisponivel;
import org.example.avaliafit.repository.FuncionarioRepository;
import org.example.avaliafit.repository.HorarioDisponivelRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/horarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HorarioDisponivelController {

    private final HorarioDisponivelRepository horarioDisponivelRepository;
    private final FuncionarioRepository funcionarioRepository;

    @GetMapping("/disponiveis")
    public ResponseEntity<List<HorarioDisponivel>> listarDisponiveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(
                horarioDisponivelRepository.findByDataAndDisponivel(data, true)
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<?> criarHorario(@RequestBody HorarioDisponivelRequestDTO dto) {
        try {
            Funcionario funcionario = funcionarioRepository.findById(dto.getIdFuncionario())
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            HorarioDisponivel novoHorario = new HorarioDisponivel();
            novoHorario.setFuncionario(funcionario);
            novoHorario.setData(dto.getData());
            novoHorario.setHorario(dto.getHorario());
            novoHorario.setDisponivel(true); // Nasce sempre disponível

            horarioDisponivelRepository.save(novoHorario);

            return ResponseEntity.ok().body("{\"mensagem\": \"Horário criado com sucesso\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"mensagem\": \"" + e.getMessage() + "\"}");
        }
    }
}