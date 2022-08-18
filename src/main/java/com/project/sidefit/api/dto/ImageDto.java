package com.project.sidefit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ImageDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageRequestDto {

        private String name;

        private String imageUrl;
    }
}
