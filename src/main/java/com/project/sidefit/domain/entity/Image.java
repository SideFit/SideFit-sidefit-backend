package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTime {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Column(length = 500)
    private String imageUrl;

    public Image(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
