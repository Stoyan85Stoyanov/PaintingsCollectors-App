package com.paintingscollectors.model.dto;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    @Expose
    private String username;
    @Expose
    private String password;
    @Expose
    private String email;
}
