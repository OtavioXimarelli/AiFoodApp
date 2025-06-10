package com.otavio.aifoodapp.dto;

import com.otavio.aifoodapp.enums.UserRoles;

public class RegisterDTO {
    private final String login;
    private final String password;
    private final UserRoles role;

    public RegisterDTO(String login, String password, UserRoles role) {
        this.login = login;
        this.password = password;
        this.role = role;
    }
    public String login() {return login;}
    public String password() { return password;
}
    public UserRoles role() {return role;}

    @Override
    public String toString() {
        return "RegisterDTO{" +
                "login='" + login + '\'' +
                ", role=" + role +
                '}';
    }
}

