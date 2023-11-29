package kz.zsanzharko.enums;

import lombok.Getter;

@Getter
public enum Lang {
    RU("RU-ru");

    private final String langText;
    Lang(String langText) {
        this.langText = langText;
    }
}
