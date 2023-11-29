package kz.zsanzharko.exception;

import lombok.Getter;

@Getter
public class InvalidProfileException extends Throwable {
    private final String clientMessage;
    private final Long chatId;
    
    public InvalidProfileException(String message, String clientMessage, Long chatId) {
        super(message);
        this.clientMessage = clientMessage;
        this.chatId = chatId;
    }
}
