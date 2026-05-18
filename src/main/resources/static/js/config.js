export const CONFIG = {
    // Se no futuro for para a cloud, muda apenas aqui!
    API_BASE_URL: 'http://localhost:8080',

    // Nomes exatos das chaves que usamos no LocalStorage
    STORAGE_KEYS: {
        TOKEN: 'token',
        ROLE: 'role',
        USER_ID: 'userId',
        USER_NAME: 'nome'
    },

    // Todos os Endpoints (URLs) do backend centralizados
    ENDPOINTS: {
        AUTH_LOGIN: '/auth/login',
        USUARIOS: '/usuarios',
        USUARIOS_FUNCIONARIOS: '/usuarios/funcionarios',
        AVALIACOES: '/avaliacoes',
        AVALIACOES_PACIENTE_ULTIMA: (id) => `/avaliacoes/paciente/${id}/ultima`,
        AVALIACOES_HISTORICO: (id) => `/avaliacoes/paciente/${id}`,
        AGENDAMENTOS: '/agendamentos',
        AGENDAMENTOS_PARA_AVALIACAO: '/agendamentos/para-avaliacao',
        HORARIOS: '/horarios',
        HORARIOS_DISPONIVEIS: (data) => `/horarios/disponiveis?data=${data}`
    }
};