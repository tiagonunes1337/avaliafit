package org.example.avaliafit.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.AvaliacaoRequestDTO;
import org.example.avaliafit.dto.AvaliacaoResponseDTO;
import org.example.avaliafit.dto.AuditoriaAvaliacaoResponseDTO;
import org.example.avaliafit.dto.AvaliacaoUpdateRequestDTO;
import org.example.avaliafit.model.Agendamento;
import org.example.avaliafit.model.Avaliacao;
import org.example.avaliafit.model.AuditoriaAvaliacao;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.Paciente;
import org.example.avaliafit.model.Usuario;
import org.example.avaliafit.repository.AgendamentoRepository;
import org.example.avaliafit.repository.AuditoriaAvaliacaoRepository;
import org.example.avaliafit.repository.AvaliacaoRepository;
import org.example.avaliafit.repository.FuncionarioRepository;
import org.example.avaliafit.repository.PacienteRepository;
import org.example.avaliafit.exception.AcessoNegadoException;
import org.example.avaliafit.exception.RegraNegocioException;
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
    private final AgendamentoRepository agendamentoRepository;
    private final AuditoriaAvaliacaoRepository auditoriaRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final AuditoriaAvaliacaoService auditoriaService;

    // == CREATE ==========================================================
    @Transactional
    public AvaliacaoResponseDTO registrar(AvaliacaoRequestDTO dto) {


        Agendamento agendamento = agendamentoRepository.findById(dto.getIdAgendamento())
                .orElseThrow(() -> new RegraNegocioException("Agendamento não encontrado."));

        if (agendamento.getAvaliacao() != null) {
            throw new RegraNegocioException("Este agendamento já possui uma avaliação registrada.");
        }

        if (dto.getAltura() == null || dto.getAltura().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("A altura deve ser maior que zero.");
        }

        BigDecimal imc = calcularImc(dto.getPeso(), dto.getAltura());

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setPaciente(agendamento.getPaciente());
        avaliacao.setFuncionario(agendamento.getFuncionario());
        avaliacao.setDataAvaliacao(LocalDateTime.now());
        avaliacao.setPeso(dto.getPeso());
        avaliacao.setAltura(dto.getAltura());
        avaliacao.setImc(imc);
        avaliacao.setPercentualGordura(dto.getPercentualGordura());
        avaliacao.setMassaMuscular(dto.getMassaMuscular());
        avaliacao.setObservacoes(dto.getObservacoes());
        avaliacao.setAgendamento(agendamento);

        avaliacaoRepository.save(avaliacao);

        agendamento.setStatus("avaliado");
        agendamentoRepository.save(agendamento);

        return toResponseDTO(avaliacao);
    }

    // == UPDATE COM AUDITORIA ============================================
    @Transactional
    public AvaliacaoResponseDTO atualizar(
            Integer idAvaliacao,
            AvaliacaoUpdateRequestDTO dto,
            Integer idFuncionarioLogado) {

        if (dto.getMotivo() == null || dto.getMotivo().isBlank()) {
            throw new RegraNegocioException("O motivo da alteração é obrigatório para fins de auditoria.");
        }

        Avaliacao avaliacaoAntiga = avaliacaoRepository.findById(idAvaliacao)
                .orElseThrow(() -> new RegraNegocioException("Avaliação não encontrada."));

        if (dto.getAltura() != null && dto.getAltura().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("A altura deve ser maior que zero.");
        }

        Funcionario funcionarioLogado = funcionarioRepository.findById(idFuncionarioLogado)
                .orElseThrow(() -> new RegraNegocioException("Funcionário logado não encontrado."));

        // Registra auditoria apenas dos campos que mudaram
        auditoriaService.registrarSeAlterado(avaliacaoAntiga, funcionarioLogado, "Peso",
                String.valueOf(avaliacaoAntiga.getPeso()), String.valueOf(dto.getPeso()), dto.getMotivo());

        auditoriaService.registrarSeAlterado(avaliacaoAntiga, funcionarioLogado, "Altura",
                String.valueOf(avaliacaoAntiga.getAltura()), String.valueOf(dto.getAltura()), dto.getMotivo());

        auditoriaService.registrarSeAlterado(avaliacaoAntiga, funcionarioLogado, "Percentual de Gordura",
                String.valueOf(avaliacaoAntiga.getPercentualGordura()), String.valueOf(dto.getPercentualGordura()), dto.getMotivo());

        auditoriaService.registrarSeAlterado(avaliacaoAntiga, funcionarioLogado, "Massa Muscular",
                String.valueOf(avaliacaoAntiga.getMassaMuscular()), String.valueOf(dto.getMassaMuscular()), dto.getMotivo());

        auditoriaService.registrarSeAlterado(avaliacaoAntiga, funcionarioLogado, "Observações",
                avaliacaoAntiga.getObservacoes(), dto.getObservacoes(), dto.getMotivo());

        // Aplica as alterações
        if (dto.getPeso() != null)              avaliacaoAntiga.setPeso(dto.getPeso());
        if (dto.getAltura() != null)            avaliacaoAntiga.setAltura(dto.getAltura());
        if (dto.getPercentualGordura() != null) avaliacaoAntiga.setPercentualGordura(dto.getPercentualGordura());
        if (dto.getMassaMuscular() != null)     avaliacaoAntiga.setMassaMuscular(dto.getMassaMuscular());
        if (dto.getObservacoes() != null)       avaliacaoAntiga.setObservacoes(dto.getObservacoes());

        // Recalcula IMC se peso ou altura mudaram
        if (dto.getPeso() != null || dto.getAltura() != null) {
            avaliacaoAntiga.setImc(calcularImc(avaliacaoAntiga.getPeso(), avaliacaoAntiga.getAltura()));
        }

        return toResponseDTO(avaliacaoRepository.save(avaliacaoAntiga));
    }

    // == AUDITORIA =======================================================
    public List<AuditoriaAvaliacaoResponseDTO> listarAuditoria(
            Integer idAvaliacao,
            Usuario usuarioLogado) {

        Avaliacao avaliacao = avaliacaoRepository.findById(idAvaliacao)
                .orElseThrow(() -> new RegraNegocioException("Avaliação não encontrada."));

        // ============================================================
        //  CORREÇÃO: AcessoNegadoException agora está importada
        //  e usa "new" corretamente
        // ============================================================
        if ("ROLE_PACIENTE".equals(usuarioLogado.getRole()) &&
                !avaliacao.getPaciente().getUsuario().getIdUsuario()
                        .equals(usuarioLogado.getIdUsuario())) {
            throw new AcessoNegadoException(
                    "Acesso negado: apenas o paciente dono ou a equipe podem ver este histórico.");
        }

        return auditoriaRepository.findByAvaliacaoOrderByDataAlteracaoDesc(avaliacao)
                .stream()
                .map(this::toAuditoriaResponseDTO)
                .toList();
    }

    // == DELETE ==========================================================
    @Transactional
    public void deletar(Integer idAvaliacao) {
        Avaliacao avaliacao = avaliacaoRepository.findById(idAvaliacao)
                .orElseThrow(() -> new RegraNegocioException("Avaliação não encontrada."));

        if (avaliacao.getAgendamento() != null) {
            agendamento(avaliacao).setStatus("agendado");
            agendamentoRepository.save(agendamento(avaliacao));
        }

        avaliacaoRepository.delete(avaliacao);
    }

    // == READ ============================================================
    public AvaliacaoResponseDTO buscarUltimaAvaliacao(Integer idUsuario) {
        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RegraNegocioException("Paciente não encontrado."));

        return avaliacaoRepository.findByPaciente(paciente)
                .stream()
                .max(Comparator.comparing(Avaliacao::getDataAvaliacao))
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RegraNegocioException("Nenhuma avaliação encontrada."));
    }

    public List<AvaliacaoResponseDTO> listarPorPaciente(Integer idUsuario) {
        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RegraNegocioException("Paciente não encontrado."));

        return avaliacaoRepository.findByPaciente(paciente)
                .stream()
                .sorted(Comparator.comparing(Avaliacao::getDataAvaliacao).reversed())
                .map(this::toResponseDTO)
                .toList();
    }

    // == HELPERS =========================================================
    private Agendamento agendamento(Avaliacao av) {
        return av.getAgendamento();
    }

    private BigDecimal calcularImc(BigDecimal peso, BigDecimal altura) {
        return peso.divide(altura.multiply(altura), 2, RoundingMode.HALF_UP);
    }

    private AvaliacaoResponseDTO toResponseDTO(Avaliacao av) {
        AvaliacaoResponseDTO dto = new AvaliacaoResponseDTO();
        dto.setIdAvaliacao(av.getIdAvaliacao());
        dto.setNomePaciente(av.getPaciente().getUsuario().getNome());
        dto.setNomeFuncionario(av.getFuncionario().getUsuario().getNome());
        dto.setDataAvaliacao(av.getDataAvaliacao());
        dto.setPeso(av.getPeso());
        dto.setAltura(av.getAltura());
        dto.setImc(av.getImc());
        dto.setPercentualGordura(av.getPercentualGordura());
        dto.setMassaMuscular(av.getMassaMuscular());
        dto.setObservacoes(av.getObservacoes());
        if (av.getAgendamento() != null) dto.setIdAgendamento(av.getAgendamento().getIdAgendamento());

        double imc = av.getImc().doubleValue();
        dto.setClassificacaoImc(classificarImc(imc));
        dto.setCorImc(corImc(imc));

        return dto;
    }

    private AuditoriaAvaliacaoResponseDTO toAuditoriaResponseDTO(AuditoriaAvaliacao auditoria) {
        AuditoriaAvaliacaoResponseDTO dto = new AuditoriaAvaliacaoResponseDTO();
        dto.setIdAuditoria(auditoria.getIdAuditoria());
        dto.setIdAvaliacao(auditoria.getAvaliacao().getIdAvaliacao());
        dto.setNomeFuncionario(auditoria.getFuncionarioQueAlterou().getUsuario().getNome());
        dto.setCargoFuncionario(auditoria.getFuncionarioQueAlterou().getCargo());
        dto.setCampoAlterado(auditoria.getCampoAlterado());
        dto.setValorAnterior(auditoria.getValorAnterior());
        dto.setValorNovo(auditoria.getValorNovo());
        dto.setDataAlteracao(auditoria.getDataAlteracao());
        dto.setMotivo(auditoria.getMotivo());
        return dto;
    }

    private String classificarImc(double imc) {
        if (imc < 18.5) return "Abaixo do peso";
        if (imc < 25.0) return "Peso normal";
        if (imc < 30.0) return "Sobrepeso";
        if (imc < 35.0) return "Obesidade grau I";
        if (imc < 40.0) return "Obesidade grau II";
        return "Obesidade grau III";
    }

    private String corImc(double imc) {
        if (imc < 18.5) return "blue";
        if (imc < 25.0) return "green";
        if (imc < 30.0) return "yellow";
        if (imc < 35.0) return "orange";
        return "red";
    }
}