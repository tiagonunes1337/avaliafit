import { CONFIG } from './config.js';

export class StorageManager {
    static setToken(token) { localStorage.setItem(CONFIG.STORAGE_KEYS.TOKEN, token); }
    static getToken() { return localStorage.getItem(CONFIG.STORAGE_KEYS.TOKEN); }

    static setRole(role) { localStorage.setItem(CONFIG.STORAGE_KEYS.ROLE, role); }
    static getRole() { return localStorage.getItem(CONFIG.STORAGE_KEYS.ROLE); }

    static setUserId(id) { localStorage.setItem(CONFIG.STORAGE_KEYS.USER_ID, id); }
    static getUserId() { return localStorage.getItem(CONFIG.STORAGE_KEYS.USER_ID); }

    // Guarda tudo de uma vez ao fazer login
    static saveLoginData(token, role, id) {
        this.setToken(token);
        this.setRole(role);
        this.setUserId(id);
    }

    // Limpa a sessão
    static clear() {
        localStorage.clear();
    }

    // Retorna 'true' se houver um token
    static isAuthenticated() {
        return !!this.getToken();
    }

    // Proteção de rotas: se não estiver logado, atira para o login
    static requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = '/login.html';
            return false;
        }
        return true;
    }

    // Limpa tudo e volta à estaca zero
    static logout() {
        this.clear();
        window.location.href = '/login.html';
    }
}