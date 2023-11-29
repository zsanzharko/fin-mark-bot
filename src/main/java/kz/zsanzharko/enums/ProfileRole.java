package kz.zsanzharko.enums;

import lombok.Getter;

public enum ProfileRole {
    USER("Пользователь"), MANAGER("Менеджер");

    @Getter
    private final String russianRole;

    ProfileRole(String russianRole) {
        this.russianRole = russianRole;
    }
}
