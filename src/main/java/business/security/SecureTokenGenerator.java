package business.security;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SecureTokenGenerator {
    private static SecureRandom rng = new SecureRandom();

    public static String generateToken() {
        return new BigInteger(130, rng).toString(32);
    }
}
