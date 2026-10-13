package edu.unimagdalena.universitystore.service;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Deja la línea de la calle con la forma que arma el formulario: "Calle 13 # 3-84".
 */
public final class StreetFormat {
    // "CALLE 13 #3-84", "avenida calle 26 # 68b - 70"
    private static final Pattern FULL = Pattern.compile(
            "^(\\p{L}+(?:\\s+\\p{L}+)?)\\s+(\\w+)\\s*#\\s*(\\w+)\\s*-\\s*(\\w+)$");
    // "calle 30": an old address that never had cross number or plate
    private static final Pattern TYPE_AND_NUMBER = Pattern.compile("^(\\p{L}+(?:\\s+\\p{L}+)?)\\s+(\\w+)$");

    private StreetFormat() {
    }

    /** Si el texto no se reconoce como una dirección, se devuelve tal cual. */
    public static String normalize(String street) {
        if (street == null) {
            return null;
        }
        String text = street.trim();
        Matcher full = FULL.matcher(text);
        if (full.matches()) {
            return capitalize(full.group(1)) + " " + full.group(2).toUpperCase()
                    + " # " + full.group(3).toUpperCase() + "-" + full.group(4).toUpperCase();
        }
        Matcher partial = TYPE_AND_NUMBER.matcher(text);
        if (partial.matches()) {
            return capitalize(partial.group(1)) + " " + partial.group(2).toUpperCase();
        }
        return text;
    }

    private static String capitalize(String words) {
        return Arrays.stream(words.trim().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
