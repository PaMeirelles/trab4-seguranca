package Model;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static Model.Constants.INTERVAL_IN_MILISSECONDS;


public class LoginModel {
    public static boolean loginStep1(String login) throws Exception {
        return DatabaseManager.loginIsNotUnique(login);
    }
    public static boolean loginStep2(String login, List<String> possiblePasswords) throws SQLException {
        String dbPass = DatabaseManager.retrievePassword(login);
        for(String password: possiblePasswords){
            if (DatabaseManager.checkPassword(password, dbPass)){
                return true;
            }
        }
        return false;
    }
    private static long getCurrentEpochInterval(){
        return System.currentTimeMillis() / INTERVAL_IN_MILISSECONDS;
    }

    private static byte[] longToBytes(long number) {
        byte[] counterBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            counterBytes[i] = (byte) (number & 0xFF);
            number >>= 8;
        }
        return counterBytes;
    }

    private static String extractCodeFromHash(byte[] hash){
        int offset = hash[hash.length - 1] & 0xf;
        long truncatedHash = 0;

        for (int i = offset; i < offset + 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[i] & 0xff);
        }

        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;

        return String.format("%06d", truncatedHash);
    }

    private static String calculateCode(String userKeyBase32, long interval) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String base32Alphabet = Base32.Alphabet.BASE32;
        boolean usePadding = true;
        boolean useLowercase = false;

        Base32 base32 = new Base32(base32Alphabet, usePadding, useLowercase);
        byte[] completeKey = base32.fromString(userKeyBase32);
        Mac hmacSha1 = Mac.getInstance(Constants.HMACSHA1);
        SecretKeySpec keySpec = new SecretKeySpec(completeKey, Constants.HMACSHA1);
        hmacSha1.init(keySpec);
        byte[] hash = hmacSha1.doFinal(longToBytes(interval));
        return extractCodeFromHash(hash);
    }

    public static boolean loginStep3(String userKeyBase32, String digits) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        long currentInterval = getCurrentEpochInterval();
        long pastInterval = getCurrentEpochInterval() - 1;
        long nextInterval = getCurrentEpochInterval() + 1;
        return Objects.equals(calculateCode(userKeyBase32, currentInterval), digits) ||
                Objects.equals(calculateCode(userKeyBase32, pastInterval), digits) ||
                Objects.equals(calculateCode(userKeyBase32, nextInterval), digits);
    }

    public static void main(String [] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String code = calculateCode("NXV37JUCLJFU7AO5NTCJ23Y5SJKQQIP4VUTCJQDF3K4BVMFWQ7QQ====", getCurrentEpochInterval());
        System.out.println(code);
    }

}
