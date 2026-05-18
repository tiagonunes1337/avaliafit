package org.example.avaliafit.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.AvaliacaoRequestDTO;
import org.example.avaliafit.dto.AvaliacaoResponseDTO;
import org.example.avaliafit.model.Agendamento;
import org.example.avaliafit.model.Avaliacao;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.Paciente;
import org.example.avaliafit.repository.AgendamentoRepository;
import org.example.avaliafit.repository.AvaliacaoRepository;
import org.example.avaliafit.repository.FuncionarioRepository;
import org.example.avaliafit.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final PacienteRepository pacienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final AgendamentoRepository agendamentoRepository;

    @Transactional
    public AvaliacaoResponseDTO registrar(AvaliacaoRequestDTO dto) {

        Agendamento agendamento = agendamentoRepository.findById(dto.getIdAgendamento())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        Paciente paciente = agendamento.getPaciente();
        Funcionario funcionario = agendamento.getFuncionario();

        if (agendamento.getAvaliacao() != null) {
            throw new RuntimeException("Este agendamento já possui uma avaliação registrada.");
        }

        if (dto.getAltura() == null || dto.getAltura().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("A altura deve ser maior que zero.");
        }

        BigDecimal alturaQuadrado = dto.getAltura().multiply(dto.getAltura());
        BigDecimal imc = dto.getPeso().divide(alturaQuadrado, 2, RoundingMode.HALF_UP);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setPaciente(paciente);
        avaliacao.setFuncionario(funcionario);
        avaliacao.setDataAvaliacao(LocalDateTime.now());
        avaliacao.setPeso(dto.getPeso());
        avaliacao.setAltura(dto.getAltura());
        avaliacao.setImc(imc);
        avaliacao.setPercentualGordura(dto.getPercentualGordura());
        avaliacao.setMassaMuscular(dto.getMassaMuscular());
        avaliacao.setObservacoes(dto.getObservacoes());
        avaliacao.setAgendamento(agendamento);

        avaliacaoRepository.save(avaliacao);

        return toResponseDTO(avaliacao);
    }

    private AvaliacaoResponseDTO toResponseDTO(Avaliacao avaliacao) {
        AvaliacaoResponseDTO response = new AvaliacaoResponseDTO();
        response.setIdAvaliacao(avaliacao.getIdAvaliacao());
        response.setNomePaciente(avaliacao.getPaciente().getUsuario().getNome());
        response.setNomeFuncionario(avaliacao.getFuncionario().getUsuario().getNome());
        response.setDataAvaliacao(avaliacao.getDataAvaliacao());
        response.setPeso(avaliacao.getPeso());
        response.setAltura(avaliacao.getAltura());
        response.setImc(avaliacao.getImc());
        response.setPercentualGordura(avaliacao.getPercentualGordura());
        response.setMassaMuscular(avaliacao.getMassaMuscular());
        response.setObservacoes(avaliacao.getObservacoes());

        if (avaliacao.getAgendamento() != null) {
            response.setIdAgendamento(avaliacao.getAgendamento().getIdAgendamento());
        }
        return response;
    }

    // 1. Busca apenas a ÚLTIMA avaliação (a mais recente) usando o ID do Usuário
    public AvaliacaoResponseDTO buscarUltimaAvaliacao(Integer idUsuario) {
        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado para este usuário"));

        return avaliacaoRepository.findByPaciente(paciente)
                .stream()
                .max(Comparator.comparing(Avaliacao::getDataAvaliacao))
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Nenhuma avaliação encontrada para este paciente."));
    }

    // 2. Lista TODAS as avaliações de um paciente (Histórico) usando o ID do Usuário
    public List<AvaliacaoResponseDTO> listarPorPaciente(Integer idUsuario) {
        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado para este usuário"));

        return avaliacaoRepository.findByPaciente(paciente)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
}