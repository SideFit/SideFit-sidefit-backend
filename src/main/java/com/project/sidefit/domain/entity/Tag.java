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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    public Tag(String name) {
        this.name = name;
    }
}
