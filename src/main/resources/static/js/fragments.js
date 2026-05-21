// static/js/fragments.js
import { StorageManager } from './storage-manager.js';
import { getColorRole } from './page-init.js';

/**
 * TEMPLATE: HTML do header (barra de navegação)
 * Será injetado dinamicamente no topo de cada página
 * O #userInfo é um placeholder para dados dinâmicos do usuário
 */
const headerTemplate = () => `
<header class="bg-gray-900 border-b-4 border-green-500 sticky top-0 z-50 p-4 shadow-lg">
    <div class="max-w-7xl mx-auto flex justify-between items-center">
        <div class="flex items-center gap-4">
            <img src="/img/logoavaliafit.png" alt="Avalia FIT Logo" class="w-28 h-auto">
            <span id="userInfo" class="text-sm text-gray-400">
                <!-- Preenchido dinamicamente por renderUserInfo() -->
            </span>
        </div>
        <nav class="hidden md:flex items-center gap-6 text-gray-300">
            <a href="/inicial.html" class="hover:text-green-400 transition font-medium">Dashboard</a>
            <a href="/cadastro.html" class="hover:text-green-400 transition font-medium">Cadastro</a>
            <a href="#" id="btnSair" class="text-red-400 hover:text-red-300 transition font-medium">Sair</a>
        </nav>
    </div>
</header>
`;

/**
 * TEMPLATE: HTML do footer (rodapé)
 * Padrão em todas as páginas protegidas
 */
const footerTemplate = () => `
<footer class="bg-gray-900 border-t border-gray-800 py-6 text-center text-gray-500 text-sm mt-auto">
    <p>&copy; 2026 Avalia FIT - Sistema em Desenvolvimento</p>
</footer>
`;

/**
 * Renderiza as informações do usuário logado no header
 * Exemplo: "Olá, João Silva | Funcionário"
 * Chamada APÓS injetar o header, para preencher o placeholder #userInfo
 */
function renderUserInfo() {
    const userName = StorageManager.getName();
    const userRole = StorageManager.getRole();
    const colorClass = getColorRole(userRole);
    
    const userInfoEl = document.getElementById('userInfo');
    if (userInfoEl) {
        // Template literal com dados do usuário buscados do localStorage
        userInfoEl.innerHTML = `
            <span class="text-white font-medium">Olá, ${userName}</span>
            <span class="px-3 py-1 rounded-full text-xs font-bold ml-2 inline-block ${colorClass}">
                ${userRole.replace('ROLE_', '')}
            </span>
        `;
    }
}

/**
 * FUNÇÃO PRINCIPAL: Injeta header e footer no DOM
 * Procura pelo <body> e adiciona:
 *   - Header no início (prepend) — aparece acima de tudo
 *   - Footer no final (append) — aparece abaixo de tudo
 * 
 * Depois renderiza as informações do usuário (nome/role) no header
 */
export function injectHeaderFooter() {
    const body = document.body;
    
    // 1️⃣ Criar elemento temporário para converter string HTML em DOM
    // (não pode injetar string diretamente, precisa de um elemento real)
    const headerContainer = document.createElement('div');
    headerContainer.innerHTML = headerTemplate();
    
    const footerContainer = document.createElement('div');
    footerContainer.innerHTML = footerTemplate();
    
    // 2️⃣ Adicionar no topo (prepend) e final (append) do body
    body.prepend(headerContainer.firstElementChild); // Extrai e injeta <header>
    body.append(footerContainer.firstElementChild);   // Extrai e injeta <footer>
    
    // 3️⃣ Renderizar dados dinâmicos (nome/role do usuário)
    renderUserInfo();
}

/**
 * Opcional: Function para atualizar o header dinamicamente
 * Caso o usuário mude dados durante a sessão, chame isso
 */
export function updateUserInfo() {
    renderUserInfo();
}
