package com.pfc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.UsuarioRepository;
import com.pfc.thindesk.service.UsuarioService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ThinDeskIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioService usuarios;
    @Autowired UsuarioRepository repository;
    @Autowired PasswordEncoder encoder;
    private static final String SENHA = "Teste-integracao-2026!";

    private String nome() { return "teste_" + UUID.randomUUID().toString().replace("-", "").substring(0,12); }

    private MockHttpSession entrar(String username) throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/login").with(csrf())
                .param("username", username).param("password", SENHA))
                .andExpect(redirectedUrl("/")).andReturn().getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    @Test
    void cadastroPublicoSeguroELoginPersistido() throws Exception {
        String username = nome();
        mvc.perform(get("/cadastro")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Crie sua conta")));
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<strong>admin</strong>"))));
        mvc.perform(post("/cadastro").param("username", username).param("password", SENHA)
                .param("confirmation", SENHA)).andExpect(status().isForbidden());
        mvc.perform(post("/cadastro").with(csrf()).param("username", "ab").param("password", SENHA)
                .param("confirmation", SENHA)).andExpect(status().isBadRequest());
        mvc.perform(post("/cadastro").with(csrf()).param("username", "admin").param("password", SENHA)
                .param("confirmation", SENHA)).andExpect(status().isBadRequest());
        mvc.perform(post("/cadastro").with(csrf()).param("username", username).param("password", "curta")
                .param("confirmation", "curta")).andExpect(status().isBadRequest());
        mvc.perform(post("/cadastro").with(csrf()).param("username", username).param("password", SENHA)
                .param("confirmation", "diferente")).andExpect(status().isBadRequest());
        mvc.perform(post("/cadastro").with(csrf()).param("username", username.toUpperCase())
                .param("password", SENHA).param("confirmation", SENHA).param("role", "ADMIN"))
                .andExpect(redirectedUrl("/login?cadastrado"));
        Usuario salvo = repository.findByUsername(username).orElseThrow();
        assertThat(salvo.getRole()).isEqualTo("USER");
        assertThat(salvo.getPasswordHash()).isNotEqualTo(SENHA);
        assertThat(encoder.matches(SENHA, salvo.getPasswordHash())).isTrue();
        mvc.perform(post("/cadastro").with(csrf()).param("username", username.toUpperCase())
                .param("password", SENHA).param("confirmation", SENHA)).andExpect(status().isBadRequest());
        mvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", "thindesk"))
                .andExpect(redirectedUrl("/login?error"));
        mvc.perform(post("/login").with(csrf()).param("username", username).param("password", "incorreta"))
                .andExpect(redirectedUrl("/login?error"));
        MockHttpSession session = entrar(username);
        mvc.perform(get("/").session(session)).andExpect(status().isOk()).andExpect(model().attribute("total", 0))
                .andExpect(content().string(containsString("Usuário comum")));
        mvc.perform(get("/clientes").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").with(csrf()).session(session)).andExpect(redirectedUrl("/login?logout"));
        // Uma nova sessão autentica a mesma conta persistida no MongoDB.
        mvc.perform(get("/").session(entrar(username))).andExpect(status().isOk());
    }

    @Test
    void usuarioComumVeApenasSeusChamadosEAdministradorResolve() throws Exception {
        String alice = nome(), bob = nome(), adminName = nome();
        usuarios.cadastrar(alice, SENHA, SENHA);
        usuarios.cadastrar(bob, SENHA, SENHA);
        Usuario admin = new Usuario();
        admin.setUsername(adminName);
        admin.setPasswordHash(encoder.encode(SENHA));
        admin.setRole("ADMIN");
        repository.save(admin);
        MockHttpSession session = entrar(alice), outra = entrar(bob);
        mvc.perform(get("/api/chamados/usuario/" + alice)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/chamados").session(session).contentType("application/json")
                .content("{\"titulo\":\"Sem token\",\"descricao\":\"Teste\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/chamados").with(csrf()).session(session).contentType("application/json")
                .content("{\"titulo\":\"\",\"descricao\":\"Teste\"}"))
                .andExpect(status().isBadRequest());
        String body = mvc.perform(post("/api/chamados").with(csrf()).session(session).contentType("application/json")
                .content("{\"titulo\":\"Chamado privado de teste\",\"descricao\":\"Solicitação de exemplo\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.usuario").value(alice))
                .andReturn().getResponse().getContentAsString();
        JsonNode chamado = json.readTree(body);
        String id = chamado.get("id").asText();
        mvc.perform(get("/").session(session)).andExpect(status().isOk()).andExpect(model().attribute("total", 1));
        mvc.perform(get("/").session(outra)).andExpect(status().isOk()).andExpect(model().attribute("total", 0))
                .andExpect(content().string(not(containsString("Chamado privado de teste"))));
        mvc.perform(get("/chamados").session(outra)).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Chamado privado de teste"))));
        mvc.perform(get("/chamados").session(session)).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("class=\"resolver-chamado\""))));
        mvc.perform(get("/api/chamados/" + id).session(outra)).andExpect(status().isForbidden());
        mvc.perform(get("/api/chamados/usuario/" + alice).session(outra)).andExpect(status().isForbidden());
        mvc.perform(put("/api/chamados/" + id + "/resolver").with(csrf()).session(session)
                .contentType("application/json").content("{\"solucao\":\"Teste\"}"))
                .andExpect(status().isForbidden());
        MockHttpSession adminSession = entrar(adminName);
        mvc.perform(get("/clientes").session(adminSession)).andExpect(status().isOk());
        mvc.perform(put("/api/chamados/" + id + "/resolver").with(csrf()).session(adminSession)
                .contentType("application/json").content("{\"solucao\":\"Funcionamento validado\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVIDO"));
        mvc.perform(get("/api/chamados/" + id).session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.solucao").value("Funcionamento validado"));
        mvc.perform(put("/api/chamados/" + id + "/resolver").with(csrf()).session(adminSession)
                .contentType("application/json").content("{\"solucao\":\"Duplicada\"}"))
                .andExpect(status().isConflict());
    }
}
