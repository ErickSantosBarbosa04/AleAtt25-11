package com.pfc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ThinDeskIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void loginDashboardAndTicketLifecycle() throws Exception {
        mvc.perform(get("/api/chamados/usuario/admin")).andExpect(status().isUnauthorized());
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Acesse a central")));
        mvc.perform(post("/login").param("username", "admin").param("password", "incorreta"))
                .andExpect(redirectedUrl("/login?error"));
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/login")
                .param("username", "admin").param("password", "thindesk"))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession(false);
        assertThat(session).isNotNull();
        mvc.perform(get("/").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dashboard de chamados")));
        mvc.perform(post("/api/chamados").session(session).contentType("application/json")
                .content("{\"titulo\":\"\",\"descricao\":\"teste\"}"))
                .andExpect(status().isBadRequest());
        String body = mvc.perform(post("/api/chamados").session(session).contentType("application/json")
                .content("{\"titulo\":\"Teste automatizado\",\"descricao\":\"Verificação do ciclo de atendimento\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ABERTO"))
                .andReturn().getResponse().getContentAsString();
        JsonNode chamado = json.readTree(body);
        String id = chamado.get("id").asText();
        mvc.perform(get("/").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Teste automatizado")));
        mvc.perform(get("/chamados").session(session)).andExpect(status().isOk());
        mvc.perform(put("/api/chamados/" + id + "/resolver").session(session).contentType("application/json")
                .content("{\"solucao\":\"Funcionamento validado\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVIDO"));
        mvc.perform(get("/api/chamados/" + id).session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.solucao").value("Funcionamento validado"));
        mvc.perform(put("/api/chamados/" + id + "/resolver").session(session).contentType("application/json")
                .content("{\"solucao\":\"Tentativa duplicada\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/logout").session(session)).andExpect(redirectedUrl("/login?logout"));
    }
}
