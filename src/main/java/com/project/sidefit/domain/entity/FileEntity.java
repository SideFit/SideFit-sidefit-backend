package com.project.sidefit.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String s3Url;

    public FileEntity(String title, String s3Url) {
        this.title = title;
        this.s3Url = s3Url;
    }

    @Override
    public String toString() {
        return "FileEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", s3Url='" + s3Url + '\'' +
                '}';
    }
}
