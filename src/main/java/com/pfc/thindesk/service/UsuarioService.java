package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public Usuario cadastrar(String username, String password, String confirmation) {
        String normalized = normalizar(username);
        if (!normalized.matches("[a-z0-9._-]{3,30}"))
            throw new IllegalArgumentException("Use de 3 a 30 caracteres no usuário: letras, números, ponto, traço ou sublinhado.");
        if (Set.of("admin", "administrator", "root", "demo").contains(normalized))
            throw new IllegalArgumentException("Esse nome de usuário é reservado. Escolha outro.");
        if (password == null || password.length() < 8 || password.length() > 64
                || password.getBytes(StandardCharsets.UTF_8).length > 72 || password.isBlank())
            throw new IllegalArgumentException("Use uma senha de 8 a 64 caracteres, com no máximo 72 bytes.");
        if (!password.equals(confirmation))
            throw new IllegalArgumentException("A confirmação da senha não confere.");
        if (repository.findByUsername(normalized).isPresent())
            throw new IllegalArgumentException("Esse usuário já existe. Escolha outro nome.");
        Usuario usuario = new Usuario();
        usuario.setUsername(normalized);
        usuario.setPasswordHash(encoder.encode(password));
        usuario.setRole("USER");
        try {
            return repository.save(usuario);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("Esse usuário já existe. Escolha outro nome.");
        }
    }

    public static String normalizar(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
