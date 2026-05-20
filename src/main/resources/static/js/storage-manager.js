import { CONFIG } from './config.js';

export class StorageManager {
    static getToken()  { return localStorage.getItem(CONFIG.STORAGE_KEYS.TOKEN); }
    static getRole()   { return localStorage.getItem(CONFIG.STORAGE_KEYS.ROLE); }
    static getUserId() { return localStorage.getItem(CONFIG.STORAGE_KEYS.USER_ID); }
    static getName()   { return localStorage.getItem(CONFIG.STORAGE_KEYS.USER_NAME); }

    /** Persiste todos os dados de sessão após o login */
    static saveLoginData(token, role, id, nome) {
        localStorage.setItem(CONFIG.STORAGE_KEYS.TOKEN,     token);
        localStorage.setItem(CONFIG.STORAGE_KEYS.ROLE,      role);
        localStorage.setItem(CONFIG.STORAGE_KEYS.USER_ID,   id);
        localStorage.setItem(CONFIG.STORAGE_KEYS.USER_NAME, nome);
    }

    static isAuthenticated() {
        return !!this.getToken();
    }

    /** Redireciona para o login se não houver sessão ativa */
    static requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = '/login.html';
            return false;
        }
        return true;
    }

    /** Limpa a sessão e redireciona para o login */
    static logout() {
        localStorage.clear();
        window.location.href = '/login.html';
    }
}
