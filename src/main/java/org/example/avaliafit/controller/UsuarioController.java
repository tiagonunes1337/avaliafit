package org.example.avaliafit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.UsuarioRequestDTO;
import org.example.avaliafit.dto.UsuarioResponseDTO;
import org.example.avaliafit.dto.UsuarioUpdateRequestDTO;
import org.example.avaliafit.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.cadastrar(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioUpdateRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorRole(@PathVariable String role) {
        return ResponseEntity.ok(usuarioService.listarPorRole(role));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}