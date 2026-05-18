package org.example.avaliafit.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.AvaliacaoRequestDTO;
import org.example.avaliafit.dto.AvaliacaoResponseDTO;
import org.example.avaliafit.model.Agendamento; // Importar Agendamento
import org.example.avaliafit.model.Avaliacao;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.Paciente;
import org.example.avaliafit.repository.AgendamentoRepository; // Importar AgendamentoRepository
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
    private final AgendamentoRepository agendamentoRepository; // NOVO: Injetar AgendamentoRepository

    @Transactional
    public AvaliacaoResponseDTO registrar(AvaliacaoRequestDTO dto) {

        // NOVO: Buscar o agendamento pelo ID
        Agendamento agendamento = agendamentoRepository.findById(dto.getIdAgendamento())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // Usar paciente e funcionário do agendamento
        Paciente paciente = agendamento.getPaciente();
        Funcionario funcionario = agendamento.getFuncionario();

        // Opcional: Validar se o agendamento já tem uma avaliação
        if (agendamento.getAvaliacao() != null) {
            throw new RuntimeException("Este agendamento já possui uma avaliação registrada.");
        }

        // Prevenção de erro: Valida se a altura é válida antes de calcular o IMC
        if (dto.getAltura() == null || dto.getAltura().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("A altura deve ser maior que zero.");
        }

        // Regra de Negócio: Cálculo do IMC = Peso / (Altura * Altura)
        BigDecimal alturaQuadrado = dto.getAltura().multiply(dto.getAltura());
        BigDecimal imc = dto.getPeso().divide(alturaQuadrado, 2, RoundingMode.HALF_UP);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setPaciente(paciente);
        avaliacao.setFuncionario(funcionario);
        avaliacao.setDataAvaliacao(LocalDateTime.now()); // Ou agendamento.getDataConsulta().atTime(agendamento.getHorario());
        avaliacao.setPeso(dto.getPeso());
        avaliacao.setAltura(dto.getAltura());
        avaliacao.setImc(imc);
        avaliacao.setPercentualGordura(dto.getPercentualGordura());
        avaliacao.setMassaMuscular(dto.getMassaMuscular());
        avaliacao.setObservacoes(dto.getObservacoes());
        avaliacao.setAgendamento(agendamento); // NOVO: Ligar a avaliação ao agendamento

        avaliacaoRepository.save(avaliacao);

        // Opcional: Atualizar o status do agendamento para 'realizado' ou 'avaliado'
        // agendamento.setStatus("realizado");
        // agendamentoRepository.save(agendamento);

        return toResponseDTO(avaliacao);
    }

    // ... (restante do código, incluindo buscarUltimaAvaliacao e listarPorPaciente)

    // Ajustar toResponseDTO para incluir informações do agendamento se necessário
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
        // Opcional: Adicionar ID do agendamento ao DTO de resposta
        if (avaliacao.getAgendamento() != null) {
            response.setIdAgendamento(avaliacao.getAgendamento().getIdAgendamento());
        }
        return response;
    }

    // 1. Busca apenas a ÚLTIMA avaliação (a mais recente)
    public AvaliacaoResponseDTO buscarUltimaAvaliacao(Integer idPaciente) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        // Retorna a avaliação com a maior data (mais recente)
        return avaliacaoRepository.findByPaciente(paciente)
                .stream()
                .max(Comparator.comparing(Avaliacao::getDataAvaliacao))
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Nenhuma avaliação encontrada para este paciente."));
    }

    // 2. Lista TODAS as avaliações de um paciente (Histórico)
    public List<AvaliacaoResponseDTO> listarPorPaciente(Integer idPaciente) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));

        return avaliacaoRepository.findByPaciente(paciente)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
}
