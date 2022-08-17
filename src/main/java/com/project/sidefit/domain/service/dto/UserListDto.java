package com.project.sidefit.domain.service.dto;

import com.project.sidefit.domain.entity.User;
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
public class UserListDto {

    private Long id;
    private String nickname;
    private String job;
    private String introduction;
    private List<String> tags;

    public UserListDto(User user){
        id = user.getId();
        nickname = user.getNickname();
        job = user.getJob();
        introduction = user.getIntroduction();
        tags = user.getTags().stream()
                .map(tag -> tag.getName()).collect(Collectors.toList());
    }
}
