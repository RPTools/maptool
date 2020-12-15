package net.rptools.maptool.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Random;

public class PasswordGenerator {
    // Uppercase L,O numbers 1 and 0 removed to try reduce ambiguity
    public static final String ELIGIBLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKMNPQRSTUVWZY2o3456789+-@#%!";
    public static final byte[] ELIGIBLE_BYTES = ELIGIBLE_CHARACTERS.getBytes(StandardCharsets.UTF_8);


    private final Random random = new SecureRandom();


    public String getPassword(int minLength, int maxLength) {
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        return getPassword(length);
    }

    public String getPassword(int length) {
        return random.ints(0,  ELIGIBLE_BYTES.length + 1)
                .limit(length)
                .map(i -> ELIGIBLE_BYTES[i])
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public String getPassword() {
        return getPassword(15, 30);
    }
}
