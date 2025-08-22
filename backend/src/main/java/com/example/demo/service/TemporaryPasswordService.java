
package com.example.demo.service;





import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import org.springframework.stereotype.Service;
import java.util.regex.Pattern;






import java.security.SecureRandom;






@Service


public class TemporaryPasswordService {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT = "0123456789";
    private static final String SPECIAL = "@$!%*?&";
    private static final String ALL = UPPER + LOWER + DIGIT + SPECIAL;

    private static final Pattern POLICY = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$");

    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public TemporaryPasswordService(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String generateTemporaryPassword() {
        return generateTemporaryPassword(12);
    }

    public String generateTemporaryPassword(int length) {
        if (length < 6 || length > 100) {
            throw new IllegalArgumentException("Password length must be between 6 and 100");
        }

        StringBuilder sb = new StringBuilder(length);
        // ensure at least one of each required class
        sb.append(pick(UPPER));
        sb.append(pick(LOWER));
        sb.append(pick(DIGIT));
        sb.append(pick(SPECIAL));

        // fill the rest from the allowed set
        for (int i = 4; i < length; i++) {
            sb.append(pick(ALL));
        }

        // shuffle
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        String candidate = new String(chars);

        // verify compliance against the same regex policy
        if (!POLICY.matcher(candidate).matches()) {
            // very unlikely, but regenerate if somehow not compliant
            return generateTemporaryPassword(length);
        }
        return candidate;
    }

    private char pick(String source) {
        return source.charAt(random.nextInt(source.length()));
    }

    // Optional helper if you ever need to hash outside UserService
    public String hashTemporaryPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    }