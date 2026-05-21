// static/js/pages/login.js
import { CONFIG } from '../config.js';
import { StorageManager } from '../storage-manager.js';

/**
 * FUNÇÃO PRINCIPAL: Executa quando o DOM carrega
 * Faz bind do formulário de login e trata o envio
 */
function initLoginPage() {
    // Buscar o formulário no HTML
    const loginForm = document.getElementById('loginForm');
    
    // Validar se encontrou (segurança contra bugs)
    if (!loginForm) {
        console.error('Formulário de login não encontrado!');
        return;
    }
    
    // Fazer bind: quando user clica "Acessar Painel", executar handleLogin
    loginForm.addEventListener('submit', handleLogin);
}

/**
 * HANDLER: Processa o envio do formulário de login
 * 
 * @param {Event} e - Evento do form submit
 */
async function handleLogin(e) {
    // Prevenir comportamento padrão (reload da página)
    e.preventDefault();
    
    // 1️⃣ CAPTURAR: Valores dos inputs do formulário
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    try {
        // 2️⃣ ENVIAR: Requisição POST ao backend para autenticar
        const response = await fetch(CONFIG.API_BASE_URL + CONFIG.ENDPOINTS.AUTH_LOGIN, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, senha: password }) // ⚠️ Backend espera 'senha'
        });

        // 3️⃣ VALIDAR: Se não ok (401, 500, etc), mostra erro ao user
        if (!response.ok) {
            alert('E-mail ou senha inválidos.');
            return;
        }

        // 4️⃣ EXTRAIR: Parse do JSON com dados do user (token, role, id, nome)
        const data = await response.json();
        
        // 5️⃣ VALIDAR SEGURANÇA: Admin só pode logar do servidor local
        // (Proteção extra: bloqueia acesso remoto para ROLE_ADMIN)
        const hostAtual = window.location.hostname;
        const isLocal = hostAtual === 'localhost' || hostAtual === '127.0.0.1';
        const isAdmin = data.role === 'ROLE_ADMIN' || data.role === 'ADMIN';
        
        if (isAdmin && !isLocal) {
            // Admin tentou logar remotamente — bloquia imediatamente!
            alert('Acesso negado: Administradores só podem fazer login a partir do servidor físico.');
            return;
        }
        
        // 6️⃣ PERSISTIR: Salvar token + dados no localStorage para futuras requisições
        StorageManager.saveLoginData(data.token, data.role, data.id, data.nome);
        
        // 7️⃣ REDIRECIONAR: Enviar user autenticado para o dashboard (inicial.html)
        window.location.href = '/inicial.html';
        
    } catch (error) {
        // Tratamento de erro (problema de rede, JSON parsing error, etc)
        console.error('Erro no login:', error);
        alert('Não foi possível conectar ao servidor. Verifique sua conexão.');
    }
}

// ✅ EXECUTAR: Quando o script carregar, inicializa a página
initLoginPage();
