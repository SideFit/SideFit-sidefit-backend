package com.project.sidefit.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotValidResponse {

    private boolean success;
    private int code;
    private List<Failure> errors;
}
