package kz.zsanzharko.enums;

import lombok.Getter;

@Getter
public enum CommandType {
    NONE("none"), START("start");

    private final String command;

    CommandType(String command) {
        this.command = command;
    }
}
