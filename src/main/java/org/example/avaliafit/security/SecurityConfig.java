package org.example.avaliafit.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
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
    public SecurityFilterChain filterChain(HttpSecurity http ) throws Exception {
        http
                .csrf(csrf -> csrf.disable( ))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Arquivos estáticos e telas públicas liberadas (COM O /error)
                        .requestMatchers("/", "/*.html", "/index.html", "/login.html", "/cadastro.html",
                                "/marcar.html", "/inicial.html", "/listarusuarios.html",
                                "/editarusuario.html", "/registraravaliacao.html", "/gerenciarhorarios.html",
                                "/static/style.css", "/*.css", "/css/**", "/img/**", "/js/**", "/error").permitAll()

                        // 2. Rota de Login liberada
                        .requestMatchers("/auth/**").permitAll()

                        // 3. REGRAS ESPECÍFICAS PRIMEIRO (O paciente pode ver a PRÓPRIA avaliação)
                        .requestMatchers(HttpMethod.GET, "/avaliacoes/paciente/*/ultima").hasAnyRole("PACIENTE", "FUNCIONARIO", "GERENTE", "ADMIN")

                        // 4. REGRAS GENÉRICAS DEPOIS
                        .requestMatchers("/avaliacoes/**").hasAnyAuthority("ROLE_FUNCIONARIO", "ROLE_ADMIN", "ROLE_GERENTE")
                        .requestMatchers("/agendamentos/**").hasAnyAuthority("ROLE_PACIENTE", "ROLE_FUNCIONARIO", "ROLE_ADMIN")
                        .requestMatchers("/usuarios/**").authenticated()

                        // 5. Qualquer outra coisa exige login
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build( );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}