package org.example.avaliafit.controller;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.LoginRequestDTO;
import org.example.avaliafit.dto.LoginResponseDTO;
import org.example.avaliafit.model.Usuario; // Importar a classe Usuario
import org.example.avaliafit.repository.UsuarioRepository; // Importar o repositório de Usuario
import org.example.avaliafit.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth" )
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository; // Injetar UsuarioRepository

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        // Buscar o objeto Usuario completo para obter o ID
        Usuario usuarioLogado = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado após autenticação"));

        String token = jwtService.gerarToken(
                userDetails.getUsername(),
                userDetails.getAuthorities().iterator().next().getAuthority()
        );

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setNome(userDetails.getUsername());
        response.setRole(userDetails.getAuthorities().iterator().next().getAuthority());
        response.setId(usuarioLogado.getIdUsuario()); // Definir o ID do usuário

        return ResponseEntity.ok(response);
    }
}
