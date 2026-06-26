package com.dailw.event;

import com.dailw.model.entity.QuestionSubmit;
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
