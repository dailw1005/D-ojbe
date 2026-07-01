package com.ojbe.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReputationChangeEvent extends ApplicationEvent {

    private final Long userId;
    private final int amount;
    private final String reason;

    public ReputationChangeEvent(Object source, Long userId, int amount, String reason) {
        super(source);
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
    }
}
