package com.paintingscollectors.model.dto;

import com.google.gson.annotations.Expose;
import com.paintingscollectors.model.entity.enums.StyleName;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaintingSeedDTO {

    @Expose
    private String name;

    @Expose
    private String author;

    @Expose
    private StyleName style;

    @Expose
    private String imageUrl;

    @Expose
    private UUID addedBy;

    @Expose
    private boolean favorite;
}
