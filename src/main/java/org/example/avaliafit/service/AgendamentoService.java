package org.example.avaliafit.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.AgendamentoRequestDTO;
import org.example.avaliafit.dto.AgendamentoResponseDTO;
import org.example.avaliafit.model.Agendamento;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.HorarioDisponivel;
import org.example.avaliafit.model.Paciente;
import org.example.avaliafit.repository.AgendamentoRepository;
import org.example.avaliafit.repository.FuncionarioRepository;
import org.example.avaliafit.repository.HorarioDisponivelRepository;
import org.example.avaliafit.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PacienteRepository pacienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final HorarioDisponivelRepository horarioDisponivelRepository;

    @Transactional
    public AgendamentoResponseDTO agendar(AgendamentoRequestDTO dto) {

        Paciente paciente = pacienteRepository.findById(dto.getIdPaciente())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        // 1. PRIMEIRO buscamos o horário escolhido
        HorarioDisponivel slot = horarioDisponivelRepository.findById(dto.getIdHorario())
                .orElseThrow(() -> new RuntimeException("Horário não encontrado"));

        if (!slot.isDisponivel()) {
            throw new RuntimeException("Horário já ocupado");
        }

        // 2. AGORA pegamos o funcionário que está DENTRO deste horário!
        // Não precisamos mais confiar no ID que vem do front-end.
        Funcionario funcionario = slot.getFuncionario();

        Agendamento agendamento = new Agendamento();
        agendamento.setPaciente(paciente);
        agendamento.setFuncionario(funcionario);
        agendamento.setDataConsulta(dto.getDataConsulta());

        agendamento.setHorario(slot.getHorario());

        agendamento.setTipoServico(dto.getTipoServico());
        agendamento.setStatus("agendado");
        agendamento.setObservacoes(dto.getObservacoes());

        agendamentoRepository.save(agendamento);

        // Bloqueia o horário para ninguém mais pegar
        slot.setDisponivel(false);
        horarioDisponivelRepository.save(slot);

        return toResponseDTO(agendamento);
    }

    public List<AgendamentoResponseDTO> listarPorPaciente(Integer idPaciente) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        return agendamentoRepository.findByPaciente(paciente)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
    public List<AgendamentoResponseDTO> listarAgendamentosParaAvaliacao() {
        return agendamentoRepository.findByDataConsultaLessThanEqualAndAvaliacaoIsNull(LocalDate.now())
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    private AgendamentoResponseDTO toResponseDTO(Agendamento agendamento) {
        AgendamentoResponseDTO response = new AgendamentoResponseDTO();
        response.setIdAgendamento(agendamento.getIdAgendamento());
        response.setNomePaciente(agendamento.getPaciente().getUsuario().getNome());
        response.setNomeFuncionario(agendamento.getFuncionario().getUsuario().getNome());
        response.setDataConsulta(agendamento.getDataConsulta());
        response.setHorario(agendamento.getHorario());
        response.setTipoServico(agendamento.getTipoServico());
        response.setStatus(agendamento.getStatus());
        response.setObservacoes(agendamento.getObservacoes());
        return response;
    }
}