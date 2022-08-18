package com.project.sidefit.domain.repository.notification;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> events = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(String emitterId, SseEmitter emitter) {
        emitters.put(emitterId, emitter);
        return emitter;
    }

    @Override
    public void saveEventCache(String eventCacheId, Object event) {
        events.put(eventCacheId, event);
    }

    @Override
    public Map<String, SseEmitter> findEmittersWithUserId(String userId) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, Object> findEventCachesWithUserId(String userId) {
        return events.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void deleteById(String id) {
        emitters.remove(id);
    }

    @Override
    public void deleteEmittersWithMemberId(String userId) {
        emitters.keySet().stream()
                .filter(key -> key.startsWith(userId))
                .findFirst()
                .ifPresent(emitters::remove);
    }

    @Override
    public void deleteEventCachesWithMemberId(String userId) {
        events.keySet().stream()
                .filter(key -> key.startsWith(userId))
                .findFirst()
                .ifPresent(events::remove);
    }
}
