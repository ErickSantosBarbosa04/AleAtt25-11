package com.pfc.thindesk.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UsuarioViewAdvice {
    @ModelAttribute("currentUsername")
    public String currentUsername(Authentication authentication) {
        return authentication == null ? "" : authentication.getName();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
