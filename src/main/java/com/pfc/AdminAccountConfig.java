package com.pfc;

import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.UsuarioRepository;
import com.pfc.thindesk.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAccountConfig implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    @Value("${app.admin.username:}") private String username;
    @Value("${app.admin.password:}") private String password;

    public AdminAccountConfig(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (username.isBlank() || password.isBlank()) return;
        String normalized = UsuarioService.normalizar(username);
        // Nunca promove uma conta comum já existente.
        if (usuarios.findByUsername(normalized).isPresent()) return;
        if (password.length() < 12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("A senha administrativa deve ter ao menos 12 caracteres e no máximo 72 bytes.");
        Usuario admin = new Usuario();
        admin.setUsername(normalized);
        admin.setPasswordHash(encoder.encode(password));
        admin.setRole("ADMIN");
        usuarios.insert(admin);
    }
}
