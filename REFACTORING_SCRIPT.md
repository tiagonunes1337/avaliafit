# 🔧 SCRIPT AUTOMATIZADO DE REFATORAÇÃO - AVALIAFIT

> Este documento contém instruções passo a passo para refatorar o projeto e resolver o problema da dashboard

---

## ⚠️ PROBLEMA CRÍTICO: Por que os dados não aparecem na Dashboard?

### **Diagnóstico do Problema**

Em `inicial.html` (linhas 174-209), o código tenta buscar a última avaliação:

```javascript
async function carregarDashboard() {
    try {
        const response = await fetch(`http://localhost:8080/avaliacoes/paciente/${idPacienteLogado}/ultima`, {
            method: 'GET',
            headers: { 'Authorization': 'Bearer ' + token }
        });
```

**O problema está em:** O endpoint `/avaliacoes/paciente/{id}/ultima` NÃO EXISTE no seu backend!

### **Verificação dos Controllers**

**❌ NÃO ENCONTRADO em AvaliacaoController.java:**
- Não há método `@GetMapping("/paciente/{id}/ultima")`
- Não há método que retorne a última avaliação

**✅ O que existe em AvaliacaoService.java (linhas 100-111):**
```java
public AvaliacaoResponseDTO buscarUltimaAvaliacao(Integer idPaciente) {
    // Este método EXISTE no service
    // MAS não há controller expondo ele!
}
```

### **Razão pela qual Gemini não consegue resolver:**
1. Gemini vê apenas o código frontend
2. Não sabe que falta um controller expondo esse endpoint
3. Não consegue inferir a estrutura Java sem ver os controllers
4. Presume que "se o JS está chamando, o endpoint deve existir"

---

## 🎯 SOLUÇÃO COMPLETA

### **PASSO 1: Criar Controllers Ausentes**

**Arquivo:** `src/main/java/org/example/avaliafit/controller/AvaliacaoController.java`

```java
package org.example.avaliafit.controller;

import lombok.RequiredArgsConstructor;
import org.example.avaliafit.dto.AvaliacaoRequestDTO;
import org.example.avaliafit.dto.AvaliacaoResponseDTO;
import org.example.avaliafit.service.AvaliacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<AvaliacaoResponseDTO> registrarAvaliacao(@RequestBody AvaliacaoRequestDTO dto) {
        return ResponseEntity.ok(avaliacaoService.registrar(dto));
    }

    // ✅ ENDPOINT FALTANTE - Resolve o problema da dashboard
    @GetMapping("/paciente/{idPaciente}/ultima")
    public ResponseEntity<AvaliacaoResponseDTO> buscarUltimaAvaliacao(@PathVariable Integer idPaciente) {
        return ResponseEntity.ok(avaliacaoService.buscarUltimaAvaliacao(idPaciente));
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarAvaliacoesPaciente(@PathVariable Integer idPaciente) {
        return ResponseEntity.ok(avaliacaoService.listarPorPaciente(idPaciente));
    }
}
```

---

### **PASSO 2: Criar Arquivo de Configuração Centralizada**

**Arquivo:** `src/main/resources/static/js/config.js`

```javascript
/**
 * Configuração centralizada do projeto
 * Permite fácil alternância entre ambientes (dev, prod)
 */

export const CONFIG = {
    // URL da API
    API_BASE_URL: (() => {
        // Desenvolvimento
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            return 'http://localhost:8080';
        }
        // Produção
        return 'https://api.avaliafit.com.br';
    })(),

    // Chaves do localStorage
    STORAGE_KEYS: {
        TOKEN: 'token',
        ROLE: 'role',
        USER_ID: 'userId',
        USER_NAME: 'nome'
    },

    // Timeouts
    REQUEST_TIMEOUT: 5000,
    TOKEN_EXPIRY_WARNING: 300000, // 5 minutos antes de expirar

    // Endpoints
    ENDPOINTS: {
        // Auth
        LOGIN: '/auth/login',
        
        // Usuários
        USUARIOS: '/usuarios',
        USUARIOS_LISTAR: '/usuarios',
        USUARIOS_BUSCAR_ID: (id) => `/usuarios/${id}`,
        USUARIOS_ROLE: (role) => `/usuarios/role/${role}`,
        
        // Avaliações
        AVALIACOES: '/avaliacoes',
        AVALIACOES_PACIENTE_ULTIMA: (id) => `/avaliacoes/paciente/${id}/ultima`,
        AVALIACOES_PACIENTE_HISTORICO: (id) => `/avaliacoes/paciente/${id}`,
        
        // Agendamentos
        AGENDAMENTOS: '/agendamentos',
        AGENDAMENTOS_PARA_AVALIACAO: '/agendamentos/para-avaliacao',
        
        // Horários
        HORARIOS: '/horarios',
        HORARIOS_DISPONIVEIS: (data) => `/horarios/disponiveis?data=${data}`
    }
};
```

---

### **PASSO 3: Criar Cliente de API Reutilizável**

**Arquivo:** `src/main/resources/static/js/api-client.js`

```javascript
import { CONFIG } from './config.js';

/**
 * Cliente centralizado para todas as requisições da API
 * Trata autenticação, erros e timeouts automaticamente
 */
export class ApiClient {
    constructor() {
        this.baseURL = CONFIG.API_BASE_URL;
        this.timeout = CONFIG.REQUEST_TIMEOUT;
    }

    /**
     * Obtém o token do localStorage
     */
    getToken() {
        return localStorage.getItem(CONFIG.STORAGE_KEYS.TOKEN);
    }

    /**
     * Verifica se há token válido
     */
    hasValidToken() {
        return !!this.getToken();
    }

    /**
     * Headers padrão com autenticação
     */
    getHeaders(contentType = 'application/json') {
        const headers = {
            'Content-Type': contentType
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        return headers;
    }

    /**
     * Faz requisição com timeout e tratamento de erro
     */
    async request(url, options = {}) {
        const finalURL = url.startsWith('http') ? url : `${this.baseURL}${url}`;
        
        // Timeout automático
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), this.timeout);

        try {
            const response = await fetch(finalURL, {
                ...options,
                signal: controller.signal,
                headers: options.headers || this.getHeaders(options.contentType)
            });

            clearTimeout(timeoutId);

            // Se 401 ou 403, token expirou
            if (response.status === 401 || response.status === 403) {
                this.redirectToLogin();
                throw new Error('Sessão expirada. Faça login novamente.');
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw {
                    status: response.status,
                    message: errorData.mensagem || `Erro ${response.status}`,
                    data: errorData
                };
            }

            return await response.json();
        } catch (error) {
            clearTimeout(timeoutId);
            throw error;
        }
    }

    /**
     * GET com headers de autenticação
     */
    async get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    }

    /**
     * POST com dados JSON
     */
    async post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    /**
     * PUT para atualizar recursos
     */
    async put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    /**
     * DELETE para remover recursos
     */
    async delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }

    /**
     * Redireciona para login se não autenticado
     */
    redirectToLogin() {
        localStorage.clear();
        window.location.href = '/login.html';
    }
}

// Exporta instância única
export const apiClient = new ApiClient();
```

---

### **PASSO 4: Criar Gerenciador de Autenticação**

**Arquivo:** `src/main/resources/static/js/auth-manager.js`

```javascript
import { CONFIG } from './config.js';

/**
 * Gerencia autenticação centralizada
 */
export class AuthManager {
    /**
     * Verifica se usuário está autenticado
     */
    static isAuthenticated() {
        return !!localStorage.getItem(CONFIG.STORAGE_KEYS.TOKEN);
    }

    /**
     * Redireciona se não autenticado
     */
    static requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = '/login.html';
            return false;
        }
        return true;
    }

    /**
     * Verifica se tem role específica
     */
    static hasRole(...roles) {
        const userRole = localStorage.getItem(CONFIG.STORAGE_KEYS.ROLE);
        return roles.includes(userRole);
    }

    /**
     * Requer role específica (redireciona se falhar)
     */
    static requireRole(...roles) {
        if (!this.isAuthenticated()) {
            window.location.href = '/login.html';
            return false;
        }

        if (!this.hasRole(...roles)) {
            alert('Acesso Negado: Você não tem permissão para acessar esta página.');
            window.location.href = '/inicial.html';
            return false;
        }
        return true;
    }

    /**
     * Obtém ID do usuário logado
     */
    static getUserId() {
        return parseInt(localStorage.getItem(CONFIG.STORAGE_KEYS.USER_ID));
    }

    /**
     * Obtém nome do usuário logado
     */
    static getUserName() {
        return localStorage.getItem(CONFIG.STORAGE_KEYS.USER_NAME);
    }

    /**
     * Obtém role do usuário
     */
    static getRole() {
        return localStorage.getItem(CONFIG.STORAGE_KEYS.ROLE);
    }

    /**
     * Salva dados de login
     */
    static saveLoginData(token, role, userId, nome) {
        localStorage.setItem(CONFIG.STORAGE_KEYS.TOKEN, token);
        localStorage.setItem(CONFIG.STORAGE_KEYS.ROLE, role);
        localStorage.setItem(CONFIG.STORAGE_KEYS.USER_ID, userId);
        localStorage.setItem(CONFIG.STORAGE_KEYS.USER_NAME, nome);
    }

    /**
     * Faz logout
     */
    static logout() {
        localStorage.clear();
        window.location.href = '/login.html';
    }
}
```

---

### **PASSO 5: Criar Componente de Navbar Reutilizável**

**Arquivo:** `src/main/resources/static/js/navbar-loader.js`

```javascript
import { AuthManager } from './auth-manager.js';

/**
 * Carrega navbar dinamicamente em todas as páginas
 */
export function loadNavbar() {
    // Verifica autenticação
    AuthManager.requireAuth();

    const navbar = document.getElementById('navbar-container');
    if (!navbar) return;

    const role = AuthManager.getRole();
    const userName = AuthManager.getUserName();

    let navItems = `
        <nav class="hidden md:flex items-center gap-8 text-gray-300">
            <a href="/inicial.html" class="hover:text-green-400 transition font-medium">Dashboard</a>
    `;

    // Navs diferentes por role
    if (role === 'ROLE_PACIENTE') {
        navItems += `
            <a href="/marcar.html" class="hover:text-green-400 transition font-medium">Marcar Consulta</a>
            <a href="/inicial.html" class="text-red-400 hover:text-red-300 transition font-medium text-sm ml-4">Sair</a>
        `;
    } else if (role === 'ROLE_FUNCIONARIO' || role === 'ROLE_GERENTE' || role === 'ROLE_ADMIN') {
        navItems += `
            <a href="/cadastro.html" class="hover:text-green-400 transition font-medium">Novo Cadastro</a>
            <a href="/listarusuarios.html" class="hover:text-green-400 transition font-medium">Usuários</a>
            <a href="/gerenciarhorarios.html" class="hover:text-green-400 transition font-medium">Agenda</a>
            <a href="/registraravaliacao.html" class="hover:text-green-400 transition font-medium">Avaliação</a>
            <a href="javascript:void(0)" onclick="AuthManager.logout()" class="text-red-400 hover:text-red-300 transition font-medium text-sm ml-4">Sair</a>
        `;
    }

    navItems += `</nav>`;
    navbar.innerHTML = navItems;
}
```

---

### **PASSO 6: Corrigir inicial.html**

**Arquivo:** `src/main/resources/static/inicial.html` (refatorado)

Substitua as linhas 1-15 e 170-210 por:

```html
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Painel do Usuário - Avalia FIT</title>
    <link rel="stylesheet" href="style.css">
</head>
<body class="bg-gray-950 text-gray-200 min-h-screen flex flex-col">

<!-- Navbar será carregada aqui -->
<header class="bg-gray-900 border-b border-green-500 sticky top-0 z-50 p-4">
    <div class="max-w-7xl mx-auto flex justify-between items-center">
        <div class="flex items-center gap-2">
            <img src="img/logoavaliafit.png" alt="Avalia FIT Logo" class="w-28 h-auto">
        </div>
        <div id="navbar-container"></div>
        <div class="w-10 h-10 bg-gray-800 rounded-full border-2 border-green-500 overflow-hidden">
            <img src="https://ui-avatars.com/api/?name=Usuario+Fit&background=1f2937&color=22c55e" alt="Avatar">
        </div>
    </div>
</header>

<!-- [RESTO DO HTML PERMANECE IGUAL] -->
<!-- ... suas seções de dashboard aqui ... -->

<script type="module">
    import { AuthManager } from './js/auth-manager.js';
    import { apiClient } from './js/api-client.js';
    import { CONFIG } from './js/config.js';
    import { loadNavbar } from './js/navbar-loader.js';

    // 1. Valida autenticação
    AuthManager.requireAuth();

    // 2. Carrega navbar
    loadNavbar();

    // 3. Carrega dados do dashboard
    async function carregarDashboard() {
        try {
            const userId = AuthManager.getUserId();
            
            // ✅ AGORA USA O ENDPOINT CORRETO
            const avaliacao = await apiClient.get(
                CONFIG.ENDPOINTS.AVALIACOES_PACIENTE_ULTIMA(userId)
            );

            // Formata data
            const dataObj = new Date(avaliacao.dataAvaliacao);
            const dataFormatada = dataObj.toLocaleDateString('pt-BR');

            // Atualiza HTML com dados reais
            document.getElementById('dataAvaliacao').innerText = dataFormatada;
            document.getElementById('pesoAtual').innerText = avaliacao.peso + ' kg';
            document.getElementById('gorduraAtual').innerText = avaliacao.percentualGordura + ' %';
            document.getElementById('massaMagraAtual').innerText = avaliacao.massaMuscular + ' kg';

        } catch (error) {
            console.error('Erro ao buscar avaliação:', error);
            document.getElementById('dataAvaliacao').innerText = 'Nenhuma avaliação registrada ainda.';
            document.getElementById('dataAvaliacao').classList.replace('text-green-500', 'text-gray-500');
        }
    }

    // Executa ao carregar
    carregarDashboard();
</script>

</body>
</html>
```

---

### **PASSO 7: Criar Script de Migração Para Usuário Executar**

**Arquivo:** `REFACTOR_DEPLOY.sh` (script bash)

```bash
#!/bin/bash

# 🔧 Script de Refatoração Automática AvaliaFIT
# Objetivo: Criar arquivos centralizados e refatorar código duplicado

echo "📋 Iniciando refatoração do AvaliaFIT..."
echo ""

# 1. Criar diretório js se não existir
echo "✅ Criando estrutura de diretórios..."
mkdir -p src/main/resources/static/js/utils
mkdir -p src/main/resources/static/components

# 2. Verificar se arquivos já existem
if [ -f "src/main/resources/static/js/config.js" ]; then
    echo "⚠️  config.js já existe. Pulando..."
else
    echo "📝 Criando config.js..."
    # O conteúdo será criado pelo script Python/por outra IA
fi

# 3. Verificar Controllers
echo ""
echo "🔍 Verificando Controllers..."

if grep -q "avaliacoes/paciente.*ultima" src/main/java/org/example/avaliafit/controller/AvaliacaoController.java 2>/dev/null; then
    echo "✅ Endpoint /avaliacoes/paciente/{id}/ultima já existe"
else
    echo "❌ FALTANTE: Endpoint /avaliacoes/paciente/{id}/ultima"
    echo "   → Execute refactor-controllers.sh para criar"
fi

# 4. Resumo
echo ""
echo "📊 PRÓXIMOS PASSOS:"
echo "1. Copiar arquivos de: REFACTORED_FILES/"
echo "2. Atualizar Controllers com refactor-controllers.sh"
echo "3. Testar endpoints com: curl http://localhost:8080/avaliacoes/paciente/1/ultima"
echo ""
echo "✨ Refatoração iniciada!"
```

---

### **PASSO 8: Atualizar Todos os HTMLs (Padrão)**

**Template para refatorar cada HTML:**

```javascript
<!-- ANTES (10+ linhas duplicadas em cada arquivo) -->
<script>
    if (!localStorage.getItem('token')) {
        window.location.href = '/login.html';
    }
</script>

<!-- DEPOIS (1 linha - modular) -->
<script type="module">
    import { AuthManager } from './js/auth-manager.js';
    AuthManager.requireAuth();
    // ... resto do código
</script>
```

---

## 📝 RESUMO DE ARQUIVOS CRIADOS

| Arquivo | Propósito | Tamanho |
|---------|-----------|--------|
| `js/config.js` | Configuração centralizada (URLs, endpoints) | ~80 linhas |
| `js/api-client.js` | Cliente HTTP reutilizável com autenticação | ~150 linhas |
| `js/auth-manager.js` | Gerenciador de autenticação centralizado | ~100 linhas |
| `js/navbar-loader.js` | Componente navbar dinâmico | ~50 linhas |
| `AvaliacaoController.java` (atualizar) | Adicionar endpoint `/avaliacoes/paciente/{id}/ultima` | +15 linhas |
| `inicial.html` (refatorar) | Usar novo sistema modular | -30 linhas |

---

## 🎯 CHECKLIST DE IMPLEMENTAÇÃO

### Fase 1: Preparação
- [ ] Criar `js/config.js`
- [ ] Criar `js/api-client.js`
- [ ] Criar `js/auth-manager.js`
- [ ] Criar `js/navbar-loader.js`

### Fase 2: Backend
- [ ] Atualizar `AvaliacaoController.java` com endpoints faltantes
- [ ] Testar endpoints com Postman

### Fase 3: Frontend (por arquivo)
- [ ] Refatorar `inicial.html`
- [ ] Refatorar `cadastro.html`
- [ ] Refatorar `marcar.html`
- [ ] Refatorar `registraravaliacao.html`
- [ ] Refatorar `gerenciarhorarios.html`
- [ ] Refatorar `editarusuario.html`
- [ ] Refatorar `listarusuarios.html`

### Fase 4: Validação
- [ ] Testar login → dashboard
- [ ] Testar carregamento de dados
- [ ] Testar todas as navegações
- [ ] Testar em diferentes navegadores

---

## 🚨 COMANDOS PARA TESTAR

```bash
# Testar o endpoint que estava faltando
curl -X GET "http://localhost:8080/avaliacoes/paciente/1/ultima" \
  -H "Authorization: Bearer seu_token_aqui"

# Resposta esperada:
# {
#   "idAvaliacao": 1,
#   "nomePaciente": "João Silva",
#   "peso": 80.5,
#   "altura": 1.80,
#   "imc": 24.84,
#   "percentualGordura": 15.5,
#   "massaMuscular": 67.8,
#   "dataAvaliacao": "2026-05-18T14:30:00"
# }
```

---

## 💡 POR QUE ISSO RESOLVE O PROBLEMA?

### **Antes (❌ Broken)**
1. Frontend chama `GET /avaliacoes/paciente/1/ultima`
2. Backend não tem esse endpoint
3. Retorna 404 Not Found
4. Dashboard fica em branco
5. Console mostra erro
6. Gemini vê só o HTML, não sabe do backend

### **Depois (✅ Fixed)**
1. Backend expõe `@GetMapping("/paciente/{idPaciente}/ultima")`
2. Frontend chama com `apiClient.get(CONFIG.ENDPOINTS.AVALIACOES_PACIENTE_ULTIMA(userId))`
3. Backend retorna dados corretos (200 OK)
4. Dashboard renderiza com dados reais
5. Código está centralizado e reutilizável
6. Fácil de debugar e manter

---

## 🤖 Para Outra IA Continuar:

Use este prompt:

```
Você é um engenheiro de software Java/Frontend especialista.
Seu trabalho é refatorar o projeto AvaliaFIT seguindo o script em REFACTORING_SCRIPT.md.

Tarefas:
1. Criar os 4 arquivos JS listados
2. Atualizar AvaliacaoController.java com o endpoint faltante
3. Refatorar cada arquivo HTML para usar os novos módulos
4. Remover código duplicado
5. Testar endpoints

Comece criando os arquivos em ordem:
1. config.js
2. api-client.js
3. auth-manager.js
4. navbar-loader.js
```

---

**Criado em:** 2026-05-18
**Versão:** 1.0
**Status:** Pronto para implementação
