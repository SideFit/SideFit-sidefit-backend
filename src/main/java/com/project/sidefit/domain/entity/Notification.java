package com.project.sidefit.domain.entity;

import com.project.sidefit.domain.enums.NotificationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private String content;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // CHAT, PERSONAL

    private boolean isChecked; // false: 확인 x, true: 확인 o

    public Notification(User sender, User receiver, String content, NotificationType type) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.type = type;
    }

    public void check() {
        isChecked = true;
    }
}
