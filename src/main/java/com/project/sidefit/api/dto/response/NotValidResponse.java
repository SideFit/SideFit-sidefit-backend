package com.project.sidefit.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotValidResponse {

    private String defaultMessage;
    private String field;
    private Object rejectedValue;
    private String code;
}
