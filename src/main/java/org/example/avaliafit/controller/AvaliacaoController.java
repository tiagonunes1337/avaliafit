package org.example.avaliafit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.AvaliacaoRequestDTO;
import org.example.avaliafit.dto.AvaliacaoResponseDTO;
import org.example.avaliafit.dto.AuditoriaAvaliacaoResponseDTO;
import org.example.avaliafit.dto.AvaliacaoUpdateRequestDTO;
import org.example.avaliafit.model.Usuario;
import org.example.avaliafit.repository.UsuarioRepository;
import org.example.avaliafit.service.AvaliacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final UsuarioRepository usuarioRepository;

    // ────────────────────────────────────────────────────────────────────────
    // POST: Registrar nova avaliação
    // ────────────────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<?> registrar(@Valid @RequestBody AvaliacaoRequestDTO dto) {
        try {
            return ResponseEntity.ok(avaliacaoService.registrar(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PUT: Atualizar avaliação COM AUDITORIA
    // ────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<?> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody AvaliacaoUpdateRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Usuario usuarioLogado = obterUsuarioLogado(userDetails);
            Integer idFuncionarioLogado = usuarioLogado.getIdUsuario();
            return ResponseEntity.ok(avaliacaoService.atualizar(id, dto, idFuncionarioLogado));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("mensagem", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // GET: Ver histórico de TODAS as alterações de uma avaliação
    // ────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/auditoria")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO', 'PACIENTE')")
    public ResponseEntity<?> verAuditoria(@PathVariable Integer id,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuarioLogado = obterUsuarioLogado(userDetails);
            List<AuditoriaAvaliacaoResponseDTO> historico = avaliacaoService.listarAuditoria(id, usuarioLogado);
            return ResponseEntity.ok(historico);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("mensagem", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("mensagem", e.getMessage()));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // DELETE: Deletar avaliação
    // ────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<?> deletar(@PathVariable Integer id) {
        try {
            avaliacaoService.deletar(id);
            return ResponseEntity.ok(Map.of("mensagem", "Avaliação removida com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // GET: Última avaliação de um paciente (dashboard)
    // ────────────────────────────────────────────────────────────────────────
    @GetMapping("/paciente/{idPaciente}/ultima")
    @PreAuthorize("hasAnyRole('PACIENTE', 'FUNCIONARIO', 'GERENTE', 'ADMIN')")
    public ResponseEntity<?> buscarUltima(@PathVariable Integer idPaciente,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuarioLogado = obterUsuarioLogado(userDetails);
            validarAcessoPaciente(idPaciente, usuarioLogado);
            return ResponseEntity.ok(avaliacaoService.buscarUltimaAvaliacao(idPaciente));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("mensagem", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("mensagem", e.getMessage()));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // GET: Listar todas as avaliações de um paciente (histórico)
    // ────────────────────────────────────────────────────────────────────────
    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO', 'PACIENTE')")
    public ResponseEntity<?> listarPorPaciente(@PathVariable Integer idPaciente,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuarioLogado = obterUsuarioLogado(userDetails);
            validarAcessoPaciente(idPaciente, usuarioLogado);
            return ResponseEntity.ok(avaliacaoService.listarPorPaciente(idPaciente));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("mensagem", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("mensagem", e.getMessage()));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // HELPER: Extrair usuário logado do SecurityContext/JWT
    // ────────────────────────────────────────────────────────────────────────
    /**     * Extrai o ID do usuário/funcionário logado a partir do JWT     *
     * Fluxo:     * 1. Pega o Authentication do SecurityContext     * 2. Extrai o email (getName())     * 3. Busca o usuário no BD pelo email     * 4. Retorna seu ID (que é também o ID do funcionário, pois usa @MapsId)     *
     * @return ID do usuário/funcionário logado     * @throws RuntimeException se não autenticado ou não encontrado     */
    private Usuario obterUsuarioLogado(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new RuntimeException("Usuário não autenticado.");
        }

        return usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userDetails.getUsername()));
    }

    private void validarAcessoPaciente(Integer idPaciente, Usuario usuarioLogado) {
        boolean isPaciente = usuarioLogado.getRole() != null && usuarioLogado.getRole().equals("ROLE_PACIENTE");
        if (isPaciente && !usuarioLogado.getIdUsuario().equals(idPaciente)) {
            throw new AccessDeniedException("Acesso negado: você só pode ver seus próprios dados.");
        }
    }
}