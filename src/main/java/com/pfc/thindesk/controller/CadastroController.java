package com.pfc.thindesk.controller;

import com.pfc.thindesk.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CadastroController {
    private final UsuarioService usuarios;

    public CadastroController(UsuarioService usuarios) { this.usuarios = usuarios; }

    @GetMapping("/cadastro")
    public String formulario() { return "cadastro"; }

    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam(defaultValue = "") String username,
                            @RequestParam(defaultValue = "") String password,
                            @RequestParam(defaultValue = "") String confirmation,
                            Model model, HttpServletResponse response) {
        try {
            usuarios.cadastrar(username, password, confirmation);
            return "redirect:/login?cadastrado";
        } catch (IllegalArgumentException ex) {
            response.setStatus(400);
            model.addAttribute("erro", ex.getMessage());
            model.addAttribute("username", UsuarioService.normalizar(username));
            return "cadastro";
        }
    }
}
