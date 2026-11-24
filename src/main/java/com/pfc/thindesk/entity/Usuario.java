package com.pfc.thindesk.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "usuarios")
public class Usuario {
    @Id private String username;
    private String passwordHash;
    private String role = "USER";
}
