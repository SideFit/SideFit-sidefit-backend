package com.project.sidefit.domain.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "favorite")
public class Favorite {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50)
    private String field;

    public Favorite(String field) {
        this.field = field;
    }
}
