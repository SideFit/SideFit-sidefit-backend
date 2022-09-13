package com.project.sidefit.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchCondition {

    LATEST("LATEST_ORDER", "최신 순"),
    POPULARITY("POPULARITY_ORDER", "인기 순");

    private final String key;
    private final String val;
}