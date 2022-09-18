package com.project.sidefit.domain.service.dto;

import com.project.sidefit.domain.entity.Mbti;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    // 직무
    private String job;

    // 한 줄 자기소개
    private String introduction;

    // 태그
    private List<String> tags;

    // 현재상태
    private List<String> currentStatuses;

    // MBTI
    private Mbti mbti;

    // 관심분야
    private List<String> favorites;

    // 기술스택
    private List<String> teches;

    // URL
    private List<PortfolioDto> portfolios;
}
