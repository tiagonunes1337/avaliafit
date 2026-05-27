package org.example.avaliafit.controller;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.UsuarioRequestDTO;
import org.example.avaliafit.dto.UsuarioResponseDTO;
import org.example.avaliafit.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importante para as permissões
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        try {
            return ResponseEntity.ok(usuarioService.cadastrar(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensagem", e.getMessage()));
        }
    }

    // 1. LISTA COMPLETA: Apenas Admin e Gerente acessam
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // 2. LISTA FILTRADA: Nutricionistas acessam para ver apenas pacientes
    @GetMapping("/pacientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarApenasPacientes() {
        return ResponseEntity.ok(usuarioService.listarPorRole("ROLE_PACIENTE"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody UsuarioRequestDTO dto) {
        try {
            return ResponseEntity.ok(usuarioService.atualizar(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }
    // 3. NOVO ENDPOINT: Devolve APENAS quem trabalha na clínica (Equipe Médica/Admin)
    @GetMapping("/funcionarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarApenasEquipe() {
        // Pega todos e o Java filtra quem NÃO é paciente antes de mandar pra internet
        List<UsuarioResponseDTO> equipe = usuarioService.listarTodos().stream()
                .filter(u -> !u.getRole().equals("ROLE_PACIENTE"))
                .toList();
        return ResponseEntity.ok(equipe);
    }
}