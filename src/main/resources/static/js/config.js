export const CONFIG = {
    // Isto faz o JS ler a URL que está lá na barra superior do navegador
    API_BASE_URL: window.location.protocol + '//' + window.location.hostname + ':8080',

    STORAGE_KEYS: {
        TOKEN:     'token',
        ROLE:      'role',
        USER_ID:   'userId',
        USER_NAME: 'nome'
    },

    ENDPOINTS: {
        AUTH_LOGIN:                  '/auth/login',
        USUARIOS:                    '/usuarios',
        USUARIOS_PACIENTES:          '/usuarios/pacientes',
        AVALIACOES:                  '/avaliacoes',
        AVALIACOES_PACIENTE_ULTIMA:  (id)   => `/avaliacoes/paciente/${id}/ultima`,
        AVALIACOES_PACIENTE:         (id)   => `/avaliacoes/paciente/${id}`,
        AGENDAMENTOS:                '/agendamentos',
        AGENDAMENTOS_PARA_AVALIACAO: '/agendamentos/para-avaliacao',
        HORARIOS:                    '/horarios',
        HORARIOS_DISPONIVEIS:        (data) => `/horarios/disponiveis?data=${data}`,
        PLANOS:                      '/planos',
        PLANO_ATIVO:                 (id)   => `/planos/paciente/${id}/ativo`,
        PLANOS_PACIENTE:             (id)   => `/planos/paciente/${id}`
    }
};