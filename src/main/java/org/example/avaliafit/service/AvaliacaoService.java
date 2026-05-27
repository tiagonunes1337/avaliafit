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
import org.springframework.security.access.AccessDeniedException;
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

    // ── CREATE ────────────────────────────────────────────────────────────────
    @Transactional
    public AvaliacaoResponseDTO registrar(AvaliacaoRequestDTO dto) {

        Agendamento agendamento = agendamentoRepository.findById(dto.getIdAgendamento())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));

        if (agendamento.getAvaliacao() != null) {
            throw new RuntimeException("Este agendamento já possui uma avaliação registrada.");
        }
        if (dto.getAltura() == null || dto.getAltura().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("A altura deve ser maior que zero.");
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

        // Marca o agendamento como avaliado
        agendamento.setStatus("avaliado");
        agendamentoRepository.save(agendamento);

        return toResponseDTO(avaliacao);
    }

    // ── UPDATE COM AUDITORIA ────────────────────────────────────────────────

    @Transactional
    public AvaliacaoResponseDTO atualizar(
            Integer idAvaliacao,
            AvaliacaoUpdateRequestDTO dto,
            Integer idFuncionarioLogado
    ) {

        if (dto.getMotivo() == null || dto.getMotivo().isBlank()) {
            throw new RuntimeException("O motivo da alteração é obrigatório para fins de auditoria e conformidade LGPD.");
        }


        Avaliacao avaliacaoAntiga = avaliacaoRepository.findById(idAvaliacao)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada."));


        if (dto.getAltura() != null && dto.getAltura().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("A altura deve ser maior que zero.");
        }


        Funcionario funcionarioLogado = funcionarioRepository.findById(idFuncionarioLogado)  // ✅ CORRIGIDO
                .orElseThrow(() -> new RuntimeException("Funcionário/usuário logado não encontrado."));


        registrarAlteracao(avaliacaoAntiga, "peso", avaliacaoAntiga.getPeso(),
                dto.getPeso(), funcionarioLogado, dto.getMotivo());

        registrarAlteracao(avaliacaoAntiga, "altura", avaliacaoAntiga.getAltura(),
                dto.getAltura(), funcionarioLogado, dto.getMotivo());

        registrarAlteracao(avaliacaoAntiga, "percentualGordura", avaliacaoAntiga.getPercentualGordura(),
                dto.getPercentualGordura(), funcionarioLogado, dto.getMotivo());

        registrarAlteracao(avaliacaoAntiga, "massaMuscular", avaliacaoAntiga.getMassaMuscular(),
                dto.getMassaMuscular(), funcionarioLogado, dto.getMotivo());

        registrarAlteracao(avaliacaoAntiga, "observacoes", avaliacaoAntiga.getObservacoes(),
                dto.getObservacoes(), funcionarioLogado, dto.getMotivo());


        if (dto.getPeso() != null) avaliacaoAntiga.setPeso(dto.getPeso());
        if (dto.getAltura() != null) avaliacaoAntiga.setAltura(dto.getAltura());
        if (dto.getPercentualGordura() != null) avaliacaoAntiga.setPercentualGordura(dto.getPercentualGordura());
        if (dto.getMassaMuscular() != null) avaliacaoAntiga.setMassaMuscular(dto.getMassaMuscular());
        if (dto.getObservacoes() != null) avaliacaoAntiga.setObservacoes(dto.getObservacoes());

        // 7️⃣ RECALCULAR IMC se peso ou altura mudaram
        if (dto.getPeso() != null || dto.getAltura() != null) {
            avaliacaoAntiga.setImc(calcularImc(avaliacaoAntiga.getPeso(), avaliacaoAntiga.getAltura()));
        }


        Avaliacao avaliacaoAtualizada = avaliacaoRepository.save(avaliacaoAntiga);

        return toResponseDTO(avaliacaoAtualizada);
    }

    private void registrarAlteracao(
            Avaliacao avaliacao,
            String campoAlterado,
            Object valorAnterior,
            Object valorNovo,
            Funcionario funcionario,
            String motivo
    ) {
        // Se ambos são null, não há alteração
        if (valorAnterior == null && valorNovo == null) {
            return;
        }

        // Se os valores são iguais, não há alteração
        if (valorAnterior != null && valorAnterior.equals(valorNovo)) {
            return;
        }

        // Criar e persistir registro de auditoria
        AuditoriaAvaliacao auditoria = new AuditoriaAvaliacao();
        auditoria.setAvaliacao(avaliacao);
        auditoria.setFuncionarioQueAlterou(funcionario);
        auditoria.setCampoAlterado(campoAlterado);
        auditoria.setValorAnterior(valorAnterior != null ? valorAnterior.toString() : "null");
        auditoria.setValorNovo(valorNovo != null ? valorNovo.toString() : "null");
        auditoria.setMotivo(motivo);
        auditoria.setDataAlteracao(LocalDateTime.now());

        auditoriaRepository.save(auditoria);

        System.out.println(" [AUDITORIA] Campo '" + campoAlterado + "' alterado por " +
                funcionario.getUsuario().getNome() + ": " +
                valorAnterior + " → " + valorNovo);
    }

    public List<AuditoriaAvaliacaoResponseDTO> listarAuditoria(Integer idAvaliacao, Usuario usuarioLogado) {
        Avaliacao avaliacao = avaliacaoRepository.findById(idAvaliacao)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada."));

        if ("ROLE_PACIENTE".equals(usuarioLogado.getRole()) &&
                !avaliacao.getPaciente().getUsuario().getIdUsuario().equals(usuarioLogado.getIdUsuario())) {
            throw new AccessDeniedException("Acesso negado: apenas o paciente dono ou a equipe de saúde podem ver este histórico.");
        }

        return auditoriaRepository.findByAvaliacaoOrderByDataAlteracaoDesc(avaliacao)
                .stream()
                .map(this::toAuditoriaResponseDTO)
                .toList();
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

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Transactional
    public void deletar(Integer idAvaliacao) {
        Avaliacao avaliacao = avaliacaoRepository.findById(idAvaliacao)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada."));

        // Reabre o agendamento para que possa ser reavaliado
        if (avaliacao.getAgendamento() != null) {
            agendamento(avaliacao).setStatus("agendado");
            agendamentoRepository.save(agendamento(avaliacao));
        }

        avaliacaoRepository.delete(avaliacao);
    }

    // ── READ: última avaliação ────────────────────────────────────────────────
    public AvaliacaoResponseDTO buscarUltimaAvaliacao(Integer idUsuario) {
        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        return avaliacaoRepository.findByPaciente(paciente)
                .stream()
                .max(Comparator.comparing(Avaliacao::getDataAvaliacao))
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Nenhuma avaliação encontrada para este paciente."));
    }

    // ── READ: histórico ───────────────────────────────────────────────────────
    public List<AvaliacaoResponseDTO> listarPorPaciente(Integer idUsuario) {
        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        return avaliacaoRepository.findByPaciente(paciente)
                .stream()
                .sorted(Comparator.comparing(Avaliacao::getDataAvaliacao).reversed())
                .map(this::toResponseDTO)
                .toList();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
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

        // Classificação e cor do IMC calculadas aqui, prontas para o front-end
        double imc = av.getImc().doubleValue();
        dto.setClassificacaoImc(classificarImc(imc));
        dto.setCorImc(corImc(imc));

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

    // Cores em string para facilitar o uso no Tailwind via JS
    private String corImc(double imc) {
        if (imc < 18.5) return "blue";
        if (imc < 25.0) return "green";
        if (imc < 30.0) return "yellow";
        if (imc < 35.0) return "orange";
        return "red";
    }
}