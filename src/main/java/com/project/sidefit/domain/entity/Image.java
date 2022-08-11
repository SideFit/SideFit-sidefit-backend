package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public Image(String name) {
        this.name = name;
    }
}
