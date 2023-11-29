/*
 * Copyright (c) 2023.
 */

package kz.zsanzharko.exception;

import lombok.Getter;

public class IncorrectAnswerForQuestionException extends Throwable {
    @Getter
    private final String clientMessage;

    public IncorrectAnswerForQuestionException(String message, String clientMessage) {
        super(message);
        this.clientMessage = clientMessage;
    }
}
