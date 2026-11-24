package com.pfc;

import com.pfc.thindesk.repository.UsuarioRepository;
import com.pfc.thindesk.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository repository) {
        return username -> repository.findByUsername(UsuarioService.normalizar(username))
                .map(usuario -> User.withUsername(usuario.getUsername())
                        .password(usuario.getPasswordHash()).roles(usuario.getRole()).build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/cadastro", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/chamados/*/resolver").hasRole("ADMIN")
                .requestMatchers("/clientes/**", "/api/clientes/**", "/ajustes-horarios/**", "/api/ajustes-horarios/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                request -> request.getServletPath().startsWith("/api/")))
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout"));
        return http.build();
    }
}
