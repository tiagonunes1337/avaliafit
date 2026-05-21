package org.example.avaliafit.service;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.HorarioDisponivelRequestDTO;
import org.example.avaliafit.dto.HorarioDisponivelResponseDTO;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.HorarioDisponivel;
import org.example.avaliafit.repository.FuncionarioRepository;
import org.example.avaliafit.repository.HorarioDisponivelRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HorarioDisponivelService {

    private final HorarioDisponivelRepository horarioDisponivelRepository;
    private final FuncionarioRepository funcionarioRepository;



    public List<HorarioDisponivelResponseDTO> listarDisponiveisPorData(LocalDate data) {
        return horarioDisponivelRepository.findByDataAndDisponivel(data, true)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public HorarioDisponivelResponseDTO criarHorario(HorarioDisponivelRequestDTO dto) {
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
    //Deletar um horário
    public void deletarHorario(Integer idHorario) {
        // 1. Verifica se o horário realmente existe no banco de dados
        HorarioDisponivel horario = horarioDisponivelRepository.findById(idHorario)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado ou já removido."));
        // Apaga diretamente o objeto que já está na memória
        horarioDisponivelRepository.delete(horario);
    }

    public HorarioDisponivelResponseDTO atualizarHorario(Integer idHorario, HorarioDisponivelRequestDTO dto) {
        // 1. Busca o horário real usando o ID do HORÁRIO
        HorarioDisponivel horarioDisponivel = horarioDisponivelRepository.findById(idHorario)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado no sistema."));

        // 2. Se o profissional mudou, busca o novo profissional correspondente
        Funcionario funcionario = funcionarioRepository.findById(dto.getIdFuncionario())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado no sistema."));

        // 3. Atualiza os dados da entidade com o que veio do DTO
        horarioDisponivel.setFuncionario(funcionario);
        horarioDisponivel.setHorario(dto.getHorario());
        horarioDisponivel.setData(dto.getData());

        horarioDisponivelRepository.save(horarioDisponivel);

        return toResponseDTO(horarioDisponivel);
    }


    private HorarioDisponivelResponseDTO toResponseDTO(HorarioDisponivel horario) {
        HorarioDisponivelResponseDTO dto = new HorarioDisponivelResponseDTO();
        dto.setIdHorario(horario.getIdHorario());
        dto.setData(horario.getData());
        dto.setHorario(horario.getHorario());
        dto.setDisponivel(horario.isDisponivel());

        var funcDTO = new HorarioDisponivelResponseDTO.FuncionarioInfoDTO();
        var usuarioDTO = new HorarioDisponivelResponseDTO.FuncionarioInfoDTO.UsuarioInfoDTO();

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