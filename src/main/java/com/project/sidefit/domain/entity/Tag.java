package com.project.sidefit.domain.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Entity
@Builder
@Table(name = "tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50)
    private String name;

    public Tag(String name) {
        this.name = name;
    }
}
