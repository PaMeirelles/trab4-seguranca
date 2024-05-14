package Model;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;


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
        return System.currentTimeMillis() / 30;
    }

    private static byte[] longToBytes(long number){
        // TODO
        return new byte[]{};
    }

    private static String extractCodeFromHash(byte[] hash){
        // TODO
        return "";
    }

    private static String calculateCode(String userKeyBase32, long interval) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String completeKey = userKeyBase32 + interval;
        Mac hmacSha1 = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(completeKey.getBytes(), "RAW");
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

}
