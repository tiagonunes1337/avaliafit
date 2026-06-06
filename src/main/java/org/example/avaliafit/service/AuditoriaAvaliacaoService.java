package org.example.avaliafit.service;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.model.AuditoriaAvaliacao;
import org.example.avaliafit.model.Avaliacao;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.repository.AuditoriaAvaliacaoRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditoriaAvaliacaoService {

    private final AuditoriaAvaliacaoRepository repository;

    // Método inteligente que só salva no banco se o valor realmente tiver mudado
    public void registrarSeAlterado(Avaliacao avaliacao, Funcionario funcionario,
                                    String campo, String valorVelho, String valorNovo, String motivo) {

        // Evita NullPointer e verifica se houve alteração real
        if (valorVelho != null && valorNovo != null && !valorVelho.equals(valorNovo)) {

            AuditoriaAvaliacao auditoria = new AuditoriaAvaliacao();
            auditoria.setAvaliacao(avaliacao);
            auditoria.setFuncionarioQueAlterou(funcionario);
            auditoria.setCampoAlterado(campo);
            auditoria.setValorAnterior(valorVelho);
            auditoria.setValorNovo(valorNovo);
            auditoria.setMotivo(motivo);

            repository.save(auditoria);
        }
    }
}