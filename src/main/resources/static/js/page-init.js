// static/js/page-init.js
import { StorageManager } from './storage-manager.js';
import { injectHeaderFooter } from './fragments.js';

/**
 * Inicialização padrão de qualquer página protegida
 * 
 * @param {Object} options - Configurações da página
 * @param {string} options.requiredRole - Role mínima necessária ('ROLE_PACIENTE', 'ROLE_FUNCIONARIO', etc)
 *   Se null, qualquer usuário autenticado entra. Se 'ROLE_ADMIN', apenas admin entra.
 * @param {Function} options.onPageReady - Callback executado APÓS a página estar pronta e autenticada
 */
export function initPage(options = {}) {
    const { requiredRole = null, onPageReady = null } = options;

    // 1️⃣ DEFESA: Verificar autenticação
    // Redireciona automaticamente para login se não houver token
    if (!StorageManager.requireAuth()) {
        return; // Interrompe se não logado (requireAuth já redireciona)
    }

    // 2️⃣ DEFESA: Validar permissão de acesso
    if (requiredRole) {
        const userRole = StorageManager.getRole();
        
        // Se a página exigir um role específico e o usuário não tem, bloqueia
        if (!hasRoleAccess(userRole, requiredRole)) {
            alert(`Acesso negado: você precisa de ${requiredRole} para acessar esta página.`);
            window.location.href = '/inicial.html'; // Redireciona para dashboard
            return;
        }
    }

    // 3️⃣ INJETAR: Header + Footer dinâmico
    injectHeaderFooter();

    // 4️⃣ BIND: Botão sair (existe em todas as páginas protegidas)
    bindLogout();

    // 5️⃣ CALLBACK: Executar a lógica específica da página
    // (ex: carregar dados, montar tabelas, listeners de formulário)
    if (onPageReady && typeof onPageReady === 'function') {
        onPageReady();
    }
}

/**
 * Verifica se o role atual do usuário tem permissão
 * Exemplo: ROLE_ADMIN pode fazer tudo, ROLE_FUNCIONARIO só acessa coisas de funcionário
 */
function hasRoleAccess(userRole, requiredRole) {
    // Admin acessa tudo
    if (userRole === 'ROLE_ADMIN') return true;
    
    // Gerente acessa pages de gerente
    if (requiredRole === 'ROLE_GERENTE' && userRole === 'ROLE_GERENTE') return true;
    if (requiredRole === 'ROLE_GERENTE' && userRole === 'ROLE_ADMIN') return true;
    
    // Funcionário acessa áreas não-admin
    if (requiredRole === 'ROLE_FUNCIONARIO' && 
        (userRole === 'ROLE_FUNCIONARIO' || userRole === 'ROLE_ADMIN' || userRole === 'ROLE_GERENTE')) {
        return true;
    }
    
    // Paciente só acessa área de paciente
    if (requiredRole === 'ROLE_PACIENTE' && 
        (userRole === 'ROLE_PACIENTE' || userRole === 'ROLE_ADMIN')) {
        return true;
    }
    
    // Caso geral: role deve ser exatamente igual
    return userRole === requiredRole;
}

/**
 * Faz bind do botão "Sair" — procura por #btnSair em qualquer página
 * Se não encontrar, ignora silenciosamente (página pode não ter botão)
 */
function bindLogout() {
    const btnSair = document.getElementById('btnSair');
    if (btnSair) {
        btnSair.addEventListener('click', (e) => {
            e.preventDefault();
            StorageManager.logout(); // Limpa storage e redireciona
        });
    }
}

/**
 * Helper: mapeia role para classe Tailwind de cor
 * Reutilizável em qualquer lugar (listas, cards, badges)
 */
export function getColorRole(role) {
    const colors = {
        'ROLE_ADMIN':       'bg-red-500/20 text-red-400 border border-red-500/30',
        'ROLE_GERENTE':     'bg-orange-500/20 text-orange-400 border border-orange-500/30',
        'ROLE_FUNCIONARIO': 'bg-blue-500/20 text-blue-400 border border-blue-500/30',
        'ROLE_PACIENTE':    'bg-green-500/20 text-green-400 border border-green-500/30',
    };
    return colors[role] || 'bg-gray-500/20 text-gray-400 border border-gray-500/30';
}
