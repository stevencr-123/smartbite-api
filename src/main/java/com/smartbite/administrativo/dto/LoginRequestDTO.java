// LoginRequestDTO.java
package com.smartbite.administrativo.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email; // o email, según tu UserDetails
    private String password;
}