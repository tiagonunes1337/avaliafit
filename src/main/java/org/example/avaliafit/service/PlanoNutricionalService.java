package org.example.avaliafit.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.PlanoNutricionalRequestDTO;
import org.example.avaliafit.dto.PlanoNutricionalResponseDTO;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.Paciente;
import org.example.avaliafit.model.PlanoNutricional;
import org.example.avaliafit.repository.FuncionarioRepository;
import org.example.avaliafit.repository.PacienteRepository;
import org.example.avaliafit.repository.PlanoNutricionalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanoNutricionalService {

    private final PlanoNutricionalRepository planoNutricionalRepository;
    private final PacienteRepository         pacienteRepository;
    private final FuncionarioRepository      funcionarioRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────
    @Transactional
    public PlanoNutricionalResponseDTO criarPlano(PlanoNutricionalRequestDTO dto) {

        Paciente   paciente   = pacienteRepository.findById(dto.getIdPaciente())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));
        Funcionario funcionario = funcionarioRepository.findById(dto.getIdFuncionario())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado."));

        // Desativa plano anterior caso exista
        planoNutricionalRepository.findByPacienteAndAtivo(paciente, true)
                .ifPresent(planoAtual -> {
                    planoAtual.setAtivo(false);
                    planoNutricionalRepository.save(planoAtual);
                });

        PlanoNutricional novoPlano = new PlanoNutricional();
        novoPlano.setPaciente(paciente);
        novoPlano.setFuncionario(funcionario);
        novoPlano.setKcalDiario(dto.getKcalDiario());
        novoPlano.setProteinas(dto.getProteinas());
        novoPlano.setCarboidratos(dto.getCarboidratos());
        novoPlano.setGorduras(dto.getGorduras());
        novoPlano.setPesoObjetivo(dto.getPesoObjetivo());
        novoPlano.setMetaAguaLitros(dto.getMetaAguaLitros());
        novoPlano.setDataInicio(dto.getDataInicio());
        novoPlano.setAtivo(true); // novo plano sempre começa ativo

        return toResponseDTO(planoNutricionalRepository.save(novoPlano));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @Transactional
    public PlanoNutricionalResponseDTO atualizar(Integer idPlano, PlanoNutricionalRequestDTO dto) {
        PlanoNutricional plano = planoNutricionalRepository.findById(idPlano)
                .orElseThrow(() -> new RuntimeException("Plano nutricional não encontrado."));

        plano.setKcalDiario(dto.getKcalDiario());
        plano.setProteinas(dto.getProteinas());
        plano.setCarboidratos(dto.getCarboidratos());
        plano.setGorduras(dto.getGorduras());
        plano.setPesoObjetivo(dto.getPesoObjetivo());
        plano.setMetaAguaLitros(dto.getMetaAguaLitros());
        plano.setDataInicio(dto.getDataInicio());

        return toResponseDTO(planoNutricionalRepository.save(plano));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Transactional
    public void deletar(Integer idPlano) {
        PlanoNutricional plano = planoNutricionalRepository.findById(idPlano)
                .orElseThrow(() -> new RuntimeException("Plano nutricional não encontrado."));
        planoNutricionalRepository.delete(plano);
    }

    // ── READ: plano ativo do paciente (usado pelo dashboard) ──────────────────
    public PlanoNutricionalResponseDTO buscarPlanoAtivo(Integer idUsuario) {
        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        return planoNutricionalRepository.findByPacienteAndAtivo(paciente, true)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Nenhum plano nutricional ativo para este paciente."));
    }

    // ── READ: histórico completo de planos do paciente ────────────────────────
    public List<PlanoNutricionalResponseDTO> listarPorPaciente(Integer idUsuario) {
        Paciente paciente = pacienteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        return planoNutricionalRepository.findByPaciente(paciente)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ── MAPEAMENTO ────────────────────────────────────────────────────────────
    private PlanoNutricionalResponseDTO toResponseDTO(PlanoNutricional plano) {
        PlanoNutricionalResponseDTO dto = new PlanoNutricionalResponseDTO();
        dto.setIdPlano(plano.getIdPlano());
        dto.setKcalDiario(plano.getKcalDiario());
        dto.setProteinas(plano.getProteinas());
        dto.setCarboidratos(plano.getCarboidratos());
        dto.setGorduras(plano.getGorduras());
        dto.setPesoObjetivo(plano.getPesoObjetivo());
        dto.setMetaAguaLitros(plano.getMetaAguaLitros());
        dto.setDataInicio(plano.getDataInicio());
        dto.setAtivo(plano.isAtivo());

        PlanoNutricionalResponseDTO.PacienteInfoDTO pacienteInfo = new PlanoNutricionalResponseDTO.PacienteInfoDTO();
        pacienteInfo.setIdUsuario(plano.getPaciente().getUsuario().getIdUsuario());
        pacienteInfo.setNome(plano.getPaciente().getUsuario().getNome());
        dto.setPaciente(pacienteInfo);

        PlanoNutricionalResponseDTO.ProfissionalInfoDTO profInfo = new PlanoNutricionalResponseDTO.ProfissionalInfoDTO();
        profInfo.setIdFuncionario(plano.getFuncionario().getIdFuncionario());
        profInfo.setNome(plano.getFuncionario().getUsuario().getNome());
        dto.setProfissional(profInfo);

        return dto;
    }
}