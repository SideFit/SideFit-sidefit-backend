package com.project.sidefit.api.dto;

import com.project.sidefit.domain.entity.Mbti;
import com.project.sidefit.domain.service.dto.PortfolioDto;
import com.project.sidefit.domain.service.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

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
    private List<PortfolioRequestDto> portfolios;

    public UserDto toUserDto() {
        return UserDto.builder().job(this.job).introduction(this.introduction)
                .tags(this.tags).currentStatuses(this.currentStatuses).mbti(this.mbti)
                .favorites(this.favorites).teches(this.teches)
                .portfolios(this.portfolios.stream().map(p -> new PortfolioDto(p.getTitle(), p.getUrl())).collect(Collectors.toList()))
                .build();
    }
}
