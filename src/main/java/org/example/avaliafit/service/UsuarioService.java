package org.example.avaliafit.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.UsuarioRequestDTO;
import org.example.avaliafit.dto.UsuarioResponseDTO;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.Paciente;
import org.example.avaliafit.model.Usuario;
import org.example.avaliafit.repository.FuncionarioRepository;
import org.example.avaliafit.repository.PacienteRepository;
import org.example.avaliafit.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {

        // verifica ANTES de salvar
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }
        if (usuarioRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new RuntimeException("CPF já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setDataNascimento(dto.getDataNascimento());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setCpf(dto.getCpf());
        usuario.setTelefone(dto.getTelefone());
        usuario.setRole(dto.getRole());

        usuarioRepository.save(usuario);

        if (dto.getRole().equals("ROLE_PACIENTE")) {
            Paciente paciente = new Paciente();
            paciente.setUsuario(usuario);
            paciente.setObjetivo(dto.getObjetivo());
            pacienteRepository.save(paciente);

        } else if (dto.getRole().equals("ROLE_FUNCIONARIO") ||
                dto.getRole().equals("ROLE_GERENTE") ||
                dto.getRole().equals("ROLE_ADMIN")) {
            Funcionario funcionario = new Funcionario();
            funcionario.setUsuario(usuario);
            funcionario.setCargo(dto.getCargo());
            funcionarioRepository.save(funcionario);

        } else {
            throw new RuntimeException("Role inválido: " + dto.getRole());
        }

        return toResponseDTO(usuario);
    }
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<UsuarioResponseDTO> listarPorRole(String role) {
        return usuarioRepository.findByRole(role)
                .stream()
                .map(this::toResponseDTO) // Usa o método de conversão que você já tem
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toResponseDTO(usuario);
    }

    public void deletar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado ou já removido."));

        // Apaga diretamente o objeto que já está na memória
        usuarioRepository.delete(usuario);
    }

    // converte entidade → DTO
    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(usuario.getIdUsuario());
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setCpf(usuario.getCpf());
        response.setTelefone(usuario.getTelefone());
        response.setRole(usuario.getRole());
        return response;
    }
    @Transactional
    public UsuarioResponseDTO atualizar(Integer id, UsuarioRequestDTO dto) {
        // Busca o usuário no banco, se não achar, acusa erro
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza os dados
        usuario.setNome(dto.getNome());
        usuario.setCpf(dto.getCpf());
        usuario.setTelefone(dto.getTelefone());
        usuario.setEmail(dto.getEmail());

        // Só criptografa e muda a senha se a pessoa preencheu o campo no front-end
        if (dto.getSenha() != null && !dto.getSenha().trim().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        usuarioRepository.save(usuario);
        return toResponseDTO(usuario);
    }
}