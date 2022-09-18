package com.project.sidefit.domain.entity;

import com.project.sidefit.domain.entity.time.BaseTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String imageUrl;

    public Image(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
