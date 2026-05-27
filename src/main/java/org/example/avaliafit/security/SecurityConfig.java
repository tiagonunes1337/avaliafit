package org.example.avaliafit.security;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // 1. IP WHITELISTING PARA A ROTA ADMIN (PRIORIDADE MÁXIMA)
                        .requestMatchers("/listarusuarios.html", "/cadastro.html").access((authentication, context) -> {
                            String ipCliente = context.getRequest().getRemoteAddr();
                            boolean isLocalhost = ipCliente.equals("127.0.0.1") || ipCliente.equals("0:0:0:0:0:0:0:1");

                            // IMPORTANTE: Aqui você pode checar o IP E checar se a pessoa tem a ROLE de Admin
                            // Mas para simplificar, se bater em /usuarios, tem que estar no Localhost.
                            return new AuthorizationDecision(isLocalhost);
                        })

                        // 2. Recursos estáticos e páginas públicas
                        .requestMatchers(
                                "/", "/*.html", "/index.html", "/login.html",
                                "/marcar.html", "/inicial.html",
                                "/cadastro.html", "/listarusuarios.html", "/editarusuario.html",
                                "/registraravaliacao.html", "/gerenciarhorarios.html",
                                "/listarhorario.html", "/editarhorario.html",
                                 "/css/**", "/img/**", "/js/**", "/error"
                        ).permitAll()

                        // 3. Auth pública
                        .requestMatchers("/auth/**").permitAll()

                        // 4. Rotas específicas mais permissivas primeiro
                        .requestMatchers(HttpMethod.GET, "/avaliacoes/paciente/*/ultima")
                        .hasAnyRole("PACIENTE", "FUNCIONARIO", "GERENTE", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/planos/paciente/*/ativo")
                        .hasAnyRole("PACIENTE", "FUNCIONARIO", "GERENTE", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/planos/paciente/*")
                        .hasAnyRole("PACIENTE", "FUNCIONARIO", "GERENTE", "ADMIN")

                        // 5. Rotas genéricas por domínio
                        .requestMatchers("/avaliacoes/**")
                        .hasAnyAuthority("ROLE_FUNCIONARIO", "ROLE_ADMIN", "ROLE_GERENTE")

                        .requestMatchers("/planos/**")
                        .hasAnyAuthority("ROLE_FUNCIONARIO", "ROLE_ADMIN", "ROLE_GERENTE")

                        .requestMatchers("/agendamentos/**")
                        .hasAnyAuthority("ROLE_PACIENTE", "ROLE_FUNCIONARIO", "ROLE_ADMIN")

                        .requestMatchers("/usuarios/**").authenticated()
                        .requestMatchers("/horarios/**").authenticated()

                        // 6. Qualquer outra rota exige login
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}