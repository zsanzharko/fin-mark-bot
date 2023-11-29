/*
 * Copyright (c) 2023.
 */

package kz.zsanzharko.utils;

import java.util.regex.Pattern;

public class RegexMatcher {
    private final static String regex = "^\\d{3}[+-]\\d+$";
    private final static Pattern pattern = Pattern.compile(regex);

    public static boolean match(String input) {
        return pattern.matcher(input).matches();
    }
}
