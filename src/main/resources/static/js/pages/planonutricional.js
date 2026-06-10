import { initPage } from '../page-init.js';
import { CONFIG } from '../config.js';
import { StorageManager } from '../storage-manager.js';
import { apiClient } from '../api-client.js';


function esc(str) {
    const d = document.createElement('div');
    d.textContent = str ?? '';
    return d.innerHTML;
}


let contadorRefeicoes = 0;


initPage({
    requiredRole: 'ROLE_FUNCIONARIO', 'ROLE_ADMINISTRADOR': 'ADMINISTRADOR',
    onPageReady: carregarPagina
});


async function carregarPagina() {
    await carregarPacientes();
    definirDataHoje();
    configurarListeners();
}


async function carregarPacientes() {
    const select = document.getElementById('paciente');

    try {
        const pacientes = await apiClient.get(CONFIG.ENDPOINTS.USUARIOS_PACIENTES);

        if (pacientes.length === 0) {
            select.innerHTML = '<option value="">Nenhum paciente cadastrado</option>';
            return;
        }

        // map() monta cada <option>, join() une em uma string,
        // uma única atribuição ao innerHTML — sem loop com +=
        const opcoes = pacientes
            .map(p => `<option value="${p.id}">${esc(p.nome)} — ${esc(p.email)}</option>`)
            .join('');

        select.innerHTML = `<option value="">Selecione um paciente...</option>` + opcoes;

    } catch (error) {
        select.innerHTML = '<option value="">Erro ao carregar pacientes</option>';
        console.error('Erro ao carregar pacientes:', error);
    }
}


function definirDataHoje() {
    const hoje = new Date().toISOString().split('T')[0];
    document.getElementById('dataInicio').value = hoje;
}


function configurarListeners() {

    // Preview de macros: atualiza ao digitar em qualquer campo nutricional
    ['proteinas', 'carboidratos', 'gorduras', 'kcalDiario'].forEach(id => {
        document.getElementById(id).addEventListener('input', atualizarPreviewMacros);
    });

    // Botão de adicionar refeição
    document.getElementById('btnAdicionarRefeicao')
        .addEventListener('click', adicionarRefeicao);

    // Botão de salvar o plano completo
    document.getElementById('btnSalvarPlano')
        .addEventListener('click', salvarPlano);
}


function atualizarPreviewMacros() {
    const p  = parseFloat(document.getElementById('proteinas').value)    || 0;
    const c  = parseFloat(document.getElementById('carboidratos').value) || 0;
    const g  = parseFloat(document.getElementById('gorduras').value)     || 0;

    // Total calórico calculado dos macros
    const kcalCalculado = (p * 4) + (c * 4) + (g * 9);

    // Percentual de cada macro em relação ao total calculado
    const pPct = kcalCalculado > 0 ? Math.round((p * 4 / kcalCalculado) * 100) : 0;
    const cPct = kcalCalculado > 0 ? Math.round((c * 4 / kcalCalculado) * 100) : 0;
    const gPct = kcalCalculado > 0 ? Math.round((g * 9 / kcalCalculado) * 100) : 0;

    document.getElementById('previewProteinas').textContent    = kcalCalculado > 0 ? `${pPct}%` : '--';
    document.getElementById('previewCarboidratos').textContent = kcalCalculado > 0 ? `${cPct}%` : '--';
    document.getElementById('previewGorduras').textContent     = kcalCalculado > 0 ? `${gPct}%` : '--';
    document.getElementById('previewKcal').textContent         = kcalCalculado > 0 ? `${Math.round(kcalCalculado)} kcal` : '--';
}


function adicionarRefeicao() {
    const container = document.getElementById('containerRefeicoes');
    const msgVazia  = document.getElementById('msgSemRefeicoes');

    // Esconde a mensagem "nenhuma refeição" na primeira adição
    if (msgVazia) msgVazia.style.display = 'none';

    const idx = ++contadorRefeicoes;

    const card = document.createElement('div');
    card.id = `refeicao-${idx}`;
    card.className = 'bg-gray-800/60 border border-gray-700 rounded-xl p-5';

    card.innerHTML = `
        <div class="flex justify-between items-center mb-4">
            <h3 class="text-white font-semibold">Refeição ${idx}</h3>
            <button onclick="removerRefeicao(${idx})"
                    class="text-red-400 hover:text-red-300 text-sm font-medium transition">
                Remover
            </button>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

            <!-- Nome da refeição -->
            <div>
                <label class="block text-gray-300 text-sm font-semibold mb-1">Nome da Refeição</label>
                <select class="campo-nome-refeicao w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 transition appearance-none">
                    <option value="Café da Manhã">Café da Manhã</option>
                    <option value="Lanche da Manhã">Lanche da Manhã</option>
                    <option value="Almoço">Almoço</option>
                    <option value="Lanche da Tarde">Lanche da Tarde</option>
                    <option value="Jantar">Jantar</option>
                    <option value="Ceia">Ceia</option>
                    <option value="Pré-Treino">Pré-Treino</option>
                    <option value="Pós-Treino">Pós-Treino</option>
                </select>
            </div>

            <!-- Calorias da refeição -->
            <div>
                <label class="block text-gray-300 text-sm font-semibold mb-1">Calorias (kcal)</label>
                <input type="number" step="0.1" min="0"
                       class="campo-calorias w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 transition"
                       placeholder="Ex: 350">
            </div>

            <!-- Proteínas da refeição -->
            <div>
                <label class="block text-gray-300 text-sm font-semibold mb-1">Proteínas (g)</label>
                <input type="number" step="0.1" min="0"
                       class="campo-proteinas-ref w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 transition"
                       placeholder="Ex: 30.0">
            </div>

            <!-- Carboidratos da refeição -->
            <div>
                <label class="block text-gray-300 text-sm font-semibold mb-1">Carboidratos (g)</label>
                <input type="number" step="0.1" min="0"
                       class="campo-carboidratos-ref w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 transition"
                       placeholder="Ex: 40.0">
            </div>

            <!-- Gorduras da refeição -->
            <div>
                <label class="block text-gray-300 text-sm font-semibold mb-1">Gorduras (g)</label>
                <input type="number" step="0.1" min="0"
                       class="campo-gorduras-ref w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 transition"
                       placeholder="Ex: 12.0">
            </div>

            <!-- Taxa Metabólica Basal -->
            <div>
                <label class="block text-gray-300 text-sm font-semibold mb-1">Taxa Metabólica Basal (kcal)</label>
                <input type="number" step="0.1" min="0"
                       class="campo-tmb w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 transition"
                       placeholder="Ex: 1568.0">
            </div>

            <!-- Descrição da refeição -->
            <div class="md:col-span-2">
                <label class="block text-gray-300 text-sm font-semibold mb-1">Descrição dos Alimentos</label>
                <textarea rows="3"
                          class="campo-descricao w-full px-3 py-2 bg-gray-700 text-white border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 transition resize-none"
                          placeholder="Ex: 2 ovos mexidos, 1 fatia de pão integral, 1 xícara de café sem açúcar"></textarea>
            </div>

        </div>
    `;

    container.appendChild(card);
}


window.removerRefeicao = function(idx) {
    const card = document.getElementById(`refeicao-${idx}`);
    if (card) card.remove();

    // Se o container ficou vazio, mostra a mensagem
    const container = document.getElementById('containerRefeicoes');
    const msgVazia  = document.getElementById('msgSemRefeicoes');
    if (container.querySelectorAll('[id^="refeicao-"]').length === 0 && msgVazia) {
        msgVazia.style.display = '';
    }
};


function coletarRefeicoes() {
    const cards = document.querySelectorAll('[id^="refeicao-"]');

    return Array.from(cards).map(card => ({
        nomeRefeicao:        card.querySelector('.campo-nome-refeicao').value,
        descricao:           card.querySelector('.campo-descricao').value.trim(),
        calorias:            parseFloat(card.querySelector('.campo-calorias').value)         || 0,
        proteinas:           parseFloat(card.querySelector('.campo-proteinas-ref').value)    || 0,
        carboidratos:        parseFloat(card.querySelector('.campo-carboidratos-ref').value) || 0,
        gorduras:            parseFloat(card.querySelector('.campo-gorduras-ref').value)     || 0,
        taxaMetabolicaBasal: parseFloat(card.querySelector('.campo-tmb').value)              || 0
    }));
}

// ============================================================
//  VALIDAR CAMPOS OBRIGATÓRIOS
//
//  Retorna uma string de erro se algo estiver faltando,
//  ou null se tudo estiver válido.
//  Centralizado aqui para não poluir o salvarPlano().
// ============================================================
function validar(dados) {
    if (!dados.idPaciente)    return 'Selecione um paciente.';
    if (!dados.dataInicio)    return 'Informe a data de início.';
    if (!dados.pesoObjetivo)  return 'Informe o peso objetivo.';
    if (!dados.metaAguaLitros) return 'Informe a meta de água diária.';
    if (!dados.kcalDiario)    return 'Informe as calorias diárias.';
    if (!dados.proteinas)     return 'Informe a meta de proteínas.';
    if (!dados.carboidratos)  return 'Informe a meta de carboidratos.';
    if (!dados.gorduras)      return 'Informe a meta de gorduras.';
    return null;
}

async function salvarPlano() {
    const btn = document.getElementById('btnSalvarPlano');

    const dados = {
        idPaciente:    parseInt(document.getElementById('paciente').value),
        idFuncionario: parseInt(StorageManager.getUserId()), // ID do funcionário logado
        dataInicio:    document.getElementById('dataInicio').value,
        pesoObjetivo:  parseFloat(document.getElementById('pesoObjetivo').value),
        metaAguaLitros: parseFloat(document.getElementById('metaAgua').value),
        kcalDiario:    parseFloat(document.getElementById('kcalDiario').value),
        proteinas:     parseFloat(document.getElementById('proteinas').value),
        carboidratos:  parseFloat(document.getElementById('carboidratos').value),
        gorduras:      parseFloat(document.getElementById('gorduras').value),
        ativo:         true,
        refeicoes:     coletarRefeicoes()
    };

    // Valida antes de mandar para a API
    const erro = validar(dados);
    if (erro) {
        alert(erro);
        return;
    }

    // Feedback visual no botão durante o envio
    btn.disabled = true;
    btn.textContent = 'Salvando...';

    try {
        await apiClient.post(CONFIG.ENDPOINTS.PLANOS, dados);
        alert('Plano nutricional salvo com sucesso!');
        window.location.href = '/inicial.html';

    } catch (error) {
        alert(error.message || 'Erro ao salvar o plano. Tente novamente.');
    } finally {
        // Restaura o botão independente de sucesso ou erro
        btn.disabled = false;
        btn.textContent = 'Salvar Plano Nutricional';
    }
}