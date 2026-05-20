export const CONFIG = {
    // Mude aqui para apontar para produção
    API_BASE_URL: 'http://localhost:8080',

    STORAGE_KEYS: {
        TOKEN:     'token',
        ROLE:      'role',
        USER_ID:   'userId',
        USER_NAME: 'nome'
    },

    ENDPOINTS: {
        AUTH_LOGIN:                   '/auth/login',
        USUARIOS:                     '/usuarios',
        USUARIOS_PACIENTES:           '/usuarios/pacientes',
        AVALIACOES:                   '/avaliacoes',
        AVALIACOES_PACIENTE_ULTIMA:   (id)   => `/avaliacoes/paciente/${id}/ultima`,
        AGENDAMENTOS:                 '/agendamentos',
        AGENDAMENTOS_PARA_AVALIACAO:  '/agendamentos/para-avaliacao',
        HORARIOS:                     '/horarios',
        HORARIOS_DISPONIVEIS:         (data) => `/horarios/disponiveis?data=${data}`
    }
};
