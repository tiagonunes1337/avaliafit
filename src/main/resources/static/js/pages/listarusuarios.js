// static/js/pages/listarusuarios.js
import { initPage, getColorRole } from '../page-init.js';
import { CONFIG } from '../config.js';
import { StorageManager } from '../storage-manager.js';
import { apiClient } from '../api-client.js';

/**
 * FUNÇÃO PRINCIPAL: Carrega a lista de usuários e popula a tabela
 * Chamada por initPage() quando a página está pronta
 */
async function loadUsersList() {
    // Pegar o role do usuário logado para definir qual endpoint chamar
    const role = StorageManager.getRole();
    
    // Se for funcionário, listar só pacientes; se for admin/gerente, listar todos
    const endpoint = role === 'ROLE_FUNCIONARIO'
        ? CONFIG.ENDPOINTS.USUARIOS_PACIENTES
        : CONFIG.ENDPOINTS.USUARIOS;

    try {
        // 1️⃣ FETCH: Buscar lista de usuários do backend
        const usuarios = await apiClient.get(endpoint);
        
        // 2️⃣ RENDERIZAR: Popular a tabela com os dados
        renderUsersTable(usuarios, role);
        
    } catch (error) {
        // Tratamento de erro (rede, auth expirida, etc)
        console.error('Erro ao carregar usuários:', error);
        alert('Falha de conexão. O servidor está rodando?');
    }
}

/**
 * Renderiza a tabela de usuários dinamicamente
 * 
 * @param {Array} usuarios - Lista de usuários do backend
 * @param {string} role - Role do usuário logado (define permissões)
 */
function renderUsersTable(usuarios, role) {
    const tbody = document.getElementById('tabela-usuarios');
    
    // Validar se a tabela existe
    if (!tbody) {
        console.error('Elemento #tabela-usuarios não encontrado!');
        return;
    }
    
    // Limpar tabela antes de preencher
    tbody.innerHTML = '';

    // Verificar se há usuários
    if (usuarios.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="p-6 text-center text-gray-500">Nenhum usuário encontrado.</td></tr>`;
        return;
    }

    // Definir se usuário pode excluir (apenas Admin e Gerente)
    const podeExcluir = role === 'ROLE_ADMIN' || role === 'ROLE_GERENTE';

    // 3️⃣ LOOP: Iterar sobre cada usuário e criar uma linha da tabela
    usuarios.forEach(user => {
        // Montar botão "Excluir" ou mensagem "Sem permissão"
        const botaoExcluir = podeExcluir
            ? `<button onclick="deletarUsuario(${user.id})" class="text-red-500 hover:text-red-400 font-medium hover:underline transition">Excluir</button>`
            : `<span class="text-gray-600 text-sm">Sem permissão</span>`;

        // Montar cor do badge de role (usando helper do page-init)
        const colorClass = getColorRole(user.role);

        // 4️⃣ TEMPLATE: Criar linha HTML e adicionar à tabela
        tbody.innerHTML += `
            <tr class="border-b border-gray-800/50 hover:bg-gray-800/40 transition">
                <td class="p-4 text-white font-medium">${user.nome}</td>
                <td class="p-4 text-gray-400">${user.email}</td>
                <td class="p-4">
                    <span class="px-3 py-1 rounded-full text-xs font-bold ${colorClass}">
                        ${user.role.replace('ROLE_', '')}
                    </span>
                </td>
                <td class="p-4 text-center">
                    <a href="editarusuario.html?id=${user.id}"
                       class="text-blue-400 hover:text-blue-300 font-medium hover:underline transition mr-4">Editar</a>
                    ${botaoExcluir}
                </td>
            </tr>
        `;
    });
}

/**
 * DELETE: Função para excluir um usuário
 * Chamada quando user clica "Excluir" em uma linha
 * 
 * @param {number} id - ID do usuário a excluir
 */
window.deletarUsuario = async function(id) {
    // Pedir confirmação antes de deletar (ação irreversível)
    if (!confirm('Tem certeza que deseja excluir este usuário? Esta ação é irreversível.')) return;

    try {
        // 1️⃣ DELETE: Enviar requisição ao backend
        await apiClient.delete(`${CONFIG.ENDPOINTS.USUARIOS}/${id}`);
        
        // 2️⃣ FEEDBACK: Mostrar sucesso
        alert('Usuário excluído com sucesso!');
        
        // 3️⃣ REFRESH: Recarregar a lista de usuários
        loadUsersList();
        
    } catch (error) {
        // Tratamento de erro (sem permissão, user não existe, etc)
        console.error('Erro ao excluir usuário:', error);
        alert('Erro ao excluir usuário. Verifique suas permissões.');
    }
};

// ✅ EXECUTAR: Inicializar a página com validação de segurança
// Vai chamar initPage() que faz:
//   1. Verificar autenticação
//   2. Validar role (só funcionário, gerente ou admin)
//   3. Injetar header/footer
//   4. Fazer bind do logout
//   5. Chamar loadUsersList() quando tudo estiver pronto
initPage({
    requiredRole: 'ROLE_FUNCIONARIO', // Bloqueia pacientes
    onPageReady: loadUsersList          // Callback: carrega usuários quando página está ready
});
