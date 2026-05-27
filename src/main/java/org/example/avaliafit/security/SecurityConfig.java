package org.example.avaliafit.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// ============================================================
//  POR QUE O ADMIN NÃO CONSEGUIA ACESSAR /cadastro.html?
//
//  O problema era arquitetural — não um bug de código.
//
//  Com HTML estático, quando o browser acessa /cadastro.html
//  ele faz uma requisição HTTP simples, sem nenhum header
//  Authorization. O token JWT fica no localStorage do browser,
//  e o localStorage só é lido pelo JavaScript DEPOIS que a
//  página já carregou.
//
//  Então o Spring Security avaliava:
//    isRoleAdmin = false  (sem token no header)
//    isIpSeguro  = true   (era localhost)
//    false && true = BLOQUEADO → 403
//
//  SOLUÇÃO APLICADA (Opção 1):
//  Separar a proteção em duas camadas:
//
//  CAMADA 1 — HTML (.html): protege só por IP
//    O servidor garante que só a máquina local acessa a tela.
//    O page-init.js garante que só ADMIN logado vê o conteúdo.
//
//  CAMADA 2 — API (/usuarios/**): protege por IP + ROLE_ADMIN
//    Aqui o token JÁ EXISTE no header (o JavaScript manda).
//    Então podemos checar role + IP ao mesmo tempo.
//    Sem token de ADMIN válido, a API rejeita tudo.
//
//  RESULTADO:
//  A proteção real está na API — que é onde os dados vivem.
//  O HTML ser entregue não é problema porque sem o token
//  de ADMIN, nenhuma chamada à API vai funcionar.
// ============================================================

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // --------------------------------------------------
                // CSRF desabilitado — correto para JWT stateless
                // --------------------------------------------------
                .csrf(csrf -> csrf.disable())

                // --------------------------------------------------
                // STATELESS — sem sessão no servidor
                // Cada requisição precisa trazer o JWT no header
                // --------------------------------------------------
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ----------------------------------------------
                        // REGRA 1: PÁGINAS HTML DE ADMIN — só IP local
                        //
                        // MUDANÇA EM RELAÇÃO À VERSÃO ANTERIOR:
                        // Antes: exigia ROLE_ADMIN + IP (quebrava porque
                        //   o browser não manda token ao navegar)
                        // Agora: exige SÓ o IP local
                        //
                        // Por que isso é seguro?
                        // Duas razões:
                        //
                        // A) O page-init.js dessas páginas chama
                        //    initPage({ requiredRole: 'ROLE_ADMIN' })
                        //    Se não tiver token de ADMIN no localStorage,
                        //    redireciona para /inicial.html imediatamente.
                        //
                        // B) Mesmo que alguém burle o JS e veja o HTML,
                        //    qualquer ação (salvar, listar, deletar) chama
                        //    a API em /usuarios/** — que aí sim verifica
                        //    ROLE_ADMIN + IP (Regra 2 abaixo).
                        //    Sem token válido, a API retorna 403 e nada acontece.
                        //
                        // Resumo: HTML é a "porta de vidro" — você vê
                        // o que tem dentro mas não consegue pegar nada
                        // sem a chave (token de ADMIN válido na API).
                        // ----------------------------------------------
                        .requestMatchers(
                                "/cadastro.html",
                                "/listarusuarios.html",
                                "/editarusuario.html"
                        )
                        .access((authenticationSupplier, context) -> {
                            boolean isLocal = isLocalhostReal(context.getRequest());
                            // Só IP — sem checar role aqui
                            return new AuthorizationDecision(isLocal);
                        })

                        // ----------------------------------------------
                        // REGRA 2: API DE USUÁRIOS — IP + ROLE_ADMIN
                        //
                        // Aqui o JavaScript já carregou e manda o token
                        // no header Authorization de cada requisição.
                        // Então podemos checar role + IP corretamente.
                        //
                        // Essa é a proteção real do sistema.
                        // Qualquer operação (criar, listar, editar, deletar)
                        // passa por aqui antes de chegar no Controller.
                        // ----------------------------------------------
                        .requestMatchers("/usuarios/**")
                        .access((authenticationSupplier, context) -> {
                            Authentication autenticacao = authenticationSupplier.get();
                            HttpServletRequest request = context.getRequest();

                            // TESTE A: tem ROLE_ADMIN no token?
                            boolean isRoleAdmin = autenticacao != null &&
                                    autenticacao.getAuthorities().stream()
                                            .anyMatch(cargo ->
                                                    cargo.getAuthority().equals("ROLE_ADMIN"));

                            // TESTE B: veio do servidor local?
                            boolean isIpSeguro = isLocalhostReal(request);

                            // Os dois precisam ser true
                            return new AuthorizationDecision(isRoleAdmin && isIpSeguro);
                        })

                        // ----------------------------------------------
                        // REGRA 3: ROTAS PÚBLICAS — sem autenticação
                        //
                        // Páginas que qualquer um pode ver + arquivos
                        // estáticos (JS, CSS, imagens).
                        // /auth/** inclui o endpoint de login.
                        // ----------------------------------------------
                        .requestMatchers(
                                "/", "/index.html", "/login.html",
                                "/marcar.html", "/inicial.html",
                                "/registraravaliacao.html", "/gerenciarhorarios.html",
                                "/listarhorario.html", "/editarhorario.html",
                                "/*css/**", "/img/**", "/js/**", "/error",
                                "/auth/**"
                        ).permitAll()

                        // ----------------------------------------------
                        // REGRA 4: ROTAS ESPECÍFICAS DE PACIENTE
                        //
                        // Vêm ANTES das genéricas (/avaliacoes/**, /planos/**)
                        // porque o Spring lê na ordem — a primeira que casar vence.
                        // ----------------------------------------------

                        // Última avaliação (dashboard do paciente)
                        .requestMatchers(HttpMethod.GET, "/avaliacoes/paciente/*/ultima")
                        .hasAnyRole("PACIENTE", "FUNCIONARIO", "GERENTE", "ADMIN")

                        // Plano ativo do paciente
                        .requestMatchers(HttpMethod.GET, "/planos/paciente/*/ativo")
                        .hasAnyRole("PACIENTE", "FUNCIONARIO", "GERENTE", "ADMIN")

                        // Todos os planos do paciente
                        .requestMatchers(HttpMethod.GET, "/planos/paciente/*")
                        .hasAnyRole("PACIENTE", "FUNCIONARIO", "GERENTE", "ADMIN")

                        // ----------------------------------------------
                        // REGRA 5: ROTAS GENÉRICAS POR DOMÍNIO
                        //
                        // hasAnyRole() adiciona ROLE_ automaticamente.
                        // hasAnyRole("FUNCIONARIO") → procura ROLE_FUNCIONARIO
                        // ----------------------------------------------

                        // Avaliações: só equipe clínica
                        .requestMatchers("/avaliacoes/**")
                        .hasAnyRole("FUNCIONARIO", "ADMIN", "GERENTE")

                        // Planos: só equipe clínica
                        .requestMatchers("/planos/**")
                        .hasAnyRole("FUNCIONARIO", "ADMIN", "GERENTE")

                        // Agendamentos: paciente cria o seu, funcionário gerencia todos
                        .requestMatchers("/agendamentos/**")
                        .hasAnyRole("PACIENTE", "FUNCIONARIO", "ADMIN")

                        // Horários: qualquer pessoa logada
                        .requestMatchers("/horarios/**").authenticated()

                        // ----------------------------------------------
                        // REGRA 6: REDE DE SEGURANÇA
                        //
                        // Qualquer rota não mapeada acima exige login.
                        // Garante que rotas esquecidas nunca fiquem públicas.
                        // Sempre mantenha como última regra.
                        // ----------------------------------------------
                        .anyRequest().authenticated()
                )

                // --------------------------------------------------
                // REGISTRAR O FILTRO JWT
                //
                // O JwtFilter roda ANTES do filtro padrão do Spring.
                // Ele lê o header "Authorization: Bearer xxx",
                // valida o token e coloca o usuário no SecurityContext.
                // Sem isso, authenticationSupplier.get() nas regras
                // acima sempre retornaria null.
                // --------------------------------------------------
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ============================================================
    //  MÉTODO AUXILIAR: isLocalhostReal
    //
    //  Usa SEMPRE request.getRemoteAddr() — o IP real da conexão TCP.
    //  Esse valor vem da camada de rede do sistema operacional
    //  e NÃO pode ser forjado por um header HTTP.
    //
    //  NÃO usamos X-Forwarded-For porque qualquer um pode mandar:
    //    curl -H "X-Forwarded-For: 127.0.0.1" http://servidor/
    //  e forjar um IP local.
    //
    //  Quando montar o Nginx no homelab, configure o Spring para
    //  confiar no proxy via RemoteIpFilter — aí sim o
    //  X-Forwarded-For pode ser usado com segurança.
    // ============================================================
    private boolean isLocalhostReal(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        // 127.0.0.1       = IPv4 localhost
        // 0:0:0:0:0:0:0:1 = IPv6 localhost (equivale a ::1)
        return ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1");
    }

    // ============================================================
    //  BEAN: passwordEncoder
    //
    //  BCrypt — hash lento de propósito para dificultar força bruta.
    //  Salt aleatório automático — a mesma senha gera hashes
    //  diferentes a cada vez. Protege contra rainbow tables.
    //  NUNCA salve senhas em texto puro ou MD5.
    // ============================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ============================================================
    //  BEAN: authenticationManager
    //
    //  Usado no AuthController para verificar email + senha.
    //  Chama UserDetailsServiceImpl → busca o usuário → compara
    //  a senha com BCrypt → retorna Authentication ou lança 401.
    // ============================================================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}