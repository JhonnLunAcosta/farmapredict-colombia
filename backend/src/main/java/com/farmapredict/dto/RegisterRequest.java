package com.farmapredict.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String rol;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
