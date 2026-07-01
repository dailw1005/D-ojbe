package com.ojbe.event;

import com.ojbe.model.entity.QuestionSubmit;
import org.springframework.context.ApplicationEvent;

public class JudgeCompletedEvent extends ApplicationEvent {

    private final QuestionSubmit questionSubmit;

    public JudgeCompletedEvent(Object source, QuestionSubmit questionSubmit) {
        super(source);
        this.questionSubmit = questionSubmit;
    }

    public QuestionSubmit getQuestionSubmit() {
        return questionSubmit;
    }
}
