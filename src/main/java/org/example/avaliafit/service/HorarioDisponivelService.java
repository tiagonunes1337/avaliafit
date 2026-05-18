package org.example.avaliafit.service;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.HorarioDisponivelRequestDTO;
import org.example.avaliafit.dto.HorarioDisponivelResponseDTO;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.HorarioDisponivel;
import org.example.avaliafit.repository.FuncionarioRepository;
import org.example.avaliafit.repository.HorarioDisponivelRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HorarioDisponivelService {

    private final HorarioDisponivelRepository horarioDisponivelRepository;
    private final FuncionarioRepository funcionarioRepository;

    // Converte a lista do Banco de Dados para a lista limpa (DTO) que o Frontend precisa
    public List<HorarioDisponivelResponseDTO> listarDisponiveisPorData(LocalDate data) {
        return horarioDisponivelRepository.findByDataAndDisponivel(data, true)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public HorarioDisponivelResponseDTO criarHorario(HorarioDisponivelRequestDTO dto) {
        // Validação 1: Não criar horários no passado
        if (dto.getData().isBefore(LocalDate.now())) {
            throw new RuntimeException("Não é permitido abrir vagas em datas que já passaram.");
        }

        Funcionario funcionario = funcionarioRepository.findById(dto.getIdFuncionario())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado no sistema."));

        HorarioDisponivel novoHorario = new HorarioDisponivel();
        novoHorario.setFuncionario(funcionario);
        novoHorario.setData(dto.getData());
        novoHorario.setHorario(dto.getHorario());
        novoHorario.setDisponivel(true);

        horarioDisponivelRepository.save(novoHorario);
        return toResponseDTO(novoHorario);
    }

    // Mapeamento manual do Model para o DTO (Evita expor senhas e dados desnecessários)
    private HorarioDisponivelResponseDTO toResponseDTO(HorarioDisponivel horario) {
        HorarioDisponivelResponseDTO dto = new HorarioDisponivelResponseDTO();
        dto.setIdHorario(horario.getIdHorario());
        dto.setData(horario.getData());
        dto.setHorario(horario.getHorario());
        dto.setDisponivel(horario.isDisponivel());

        var funcDTO = new HorarioDisponivelResponseDTO.FuncionarioInfoDTO();
        var usuarioDTO = new HorarioDisponivelResponseDTO.FuncionarioInfoDTO.UsuarioInfoDTO();

        // Puxando o nome lá da tabela de usuário! É isso que o marcar.html precisa.
        usuarioDTO.setIdUsuario(horario.getFuncionario().getUsuario().getIdUsuario());
        usuarioDTO.setNome(horario.getFuncionario().getUsuario().getNome());
        usuarioDTO.setEmail(horario.getFuncionario().getUsuario().getEmail());
        usuarioDTO.setRole(horario.getFuncionario().getUsuario().getRole());

        funcDTO.setIdFuncionario(horario.getFuncionario().getIdFuncionario());
        funcDTO.setUsuario(usuarioDTO);

        dto.setFuncionario(funcDTO);
        return dto;
    }
}