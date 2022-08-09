package com.project.sidefit.domain.repository.notification;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {

    SseEmitter save(String emitterId, SseEmitter emitter);
    void saveEventCache(String eventCacheId, Object event);
    Map<String, SseEmitter> findEmittersWithUserId(String userId);
    Map<String, Object> findEventCachesWithUserId(String userId);
    void deleteById(String id);
    void deleteEmittersWithMemberId(String userId);
    void deleteEventCachesWithMemberId(String userId);
}
