package com.project.sidefit.domain.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "current_status")
public class CurrentStatus {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50)
    private String status;

    public CurrentStatus(String status) {
        this.status = status;
    }
}
