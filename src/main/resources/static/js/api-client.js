import { CONFIG } from './config.js';
import { StorageManager } from './storage-manager.js';

class ApiClient {
    constructor() {
        this.baseURL = CONFIG.API_BASE_URL;
    }

    // Injeta automaticamente o Bearer Token em todas as chamadas
    getHeaders(contentType = 'application/json') {
        const headers = {};
        if (contentType) {
            headers['Content-Type'] = contentType;
        }

        const token = StorageManager.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        return headers;
    }

    // Método central que executa os Fetchs
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;

        try {
            const response = await fetch(url, {
                ...options,
                headers: options.headers || this.getHeaders(options.contentType)
            });

            // Se o Spring Boot negar acesso (token expirado ou acesso indevido)
            if (response.status === 401 || response.status === 403) {
                StorageManager.logout();
                throw new Error('Sessão expirada ou acesso negado. Faça login novamente.');
            }

            // Tratamento genérico de erros do backend
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw {
                    status: response.status,
                    message: errorData.mensagem || `Erro no servidor (${response.status})`
                };
            }

            // Extrai o JSON automaticamente se existir
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                return await response.json();
            } else {
                return await response.text();
            }

        } catch (error) {
            console.error(`Erro na requisição API (${endpoint}):`, error);
            throw error; // Lança o erro para o HTML mostrar um "alert", se for necessário
        }
    }

    // Atalhos práticos
    async get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    }

    async post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data),
            contentType: 'application/json'
        });
    }

    async put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data),
            contentType: 'application/json'
        });
    }

    async delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
}

// Exportamos uma instância pronta a usar!
export const apiClient = new ApiClient();