package Model;

public class Constants {
    public static final String KEY_GENERATOR_ALGO = "AES";
    public static final Integer KEY_SIZE = 256;
    public static final String RSA_CYPHER = "RSA/ECB/PKCS1Padding";
    public static final String AES_CYPHER = "AES/ECB/PKCS5Padding";
    public static final String KEY_ALGO = "RSA";
    public static final Integer TEST_ARRAY_SIZE = 496;
    public static final String DIGEST_ALGO = "SHA-256";
    public static final String CERTIFICATE_TYPE = "X.509";
    public static final String CONNECTION_STRING = "jdbc:sqlite:D:/Segurança/trab4-seguranca/db.db";
    //public static final String CONNECTION_STRING = "jdbc:sqlite:../db.db";
    public static final String SECURE_RANDOM_ALGO = "SHA1PRNG";
    public static final Integer SALT_LENGTH = 16;
    public static final Integer COST_FACTOR = 12;
    public static final Integer TOTP_SIZE = 20;
    public static final long INTERVAL_IN_MILISSECONDS = 30000;
    public static final String HMACSHA1 = "HmacSHA1";
    public static final long BLOCK_TIME = 1000 * 60 * 2;
    public static final String DIGITAL_SIGNATURE_ALGO = "SHA1withRSA";
}
