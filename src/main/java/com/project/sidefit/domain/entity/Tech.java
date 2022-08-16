package com.project.sidefit.domain.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "tech")
public class Tech {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50)
    private String stack;

    public Tech(String stack) {
        this.stack = stack;
    }
}
