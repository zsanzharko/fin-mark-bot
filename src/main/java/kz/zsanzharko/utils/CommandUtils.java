package kz.zsanzharko.utils;

import kz.zsanzharko.enums.CommandType;

import java.util.Locale;

public class CommandUtils {

    public CommandType resolve(String text) {
        if(text.startsWith("/")) {
            text = text.substring(1);
            String command = text.toLowerCase(Locale.ROOT);
            for (CommandType value : CommandType.values()) {
                if (command.equals(value.getCommand())) {
                    return value;
                }
            }
            return CommandType.NONE;
        }
        return null;
    }

    public boolean isCommand(String command) {
        return command.startsWith("/");
    }
}
