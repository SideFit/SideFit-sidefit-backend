package com.project.sidefit.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchCondition {

    LATEST_ORDER("LATEST", "최신 순"),
    ACCURACY_ORDER("ACCURACY", "정확도 순");

    private final String key;
    private final String val;
}
