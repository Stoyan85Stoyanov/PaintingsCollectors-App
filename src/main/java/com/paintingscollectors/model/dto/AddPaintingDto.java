package com.paintingscollectors.model.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class AddPaintingDto {

    @NotBlank
    @Size(min = 5, max = 40)
    private String name;

    @NotBlank
    @Size(min = 5, max = 40)
    private String author;

    @NotBlank
    @Size(max = 150)
    private String imageUrl;

    @NotBlank(message = "You must select a style!")
    private String style;
}
