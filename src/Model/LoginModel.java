package Model;

import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
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
    public static String loginStep2(String login, List<String> possiblePasswords) throws SQLException {
        String dbPass = DatabaseManager.retrievePassword(login);
        for(String password: possiblePasswords){
            if (DatabaseManager.checkPassword(password, dbPass)){
                return password;
            }
        }
        return null;
    }

    public static boolean loginStep3(String userKeyBase32, String digits) throws NoSuchAlgorithmException, InvalidKeyException {
        TOTP totp = new TOTP(userKeyBase32, INTERVAL_IN_MILISSECONDS);
        return totp.validateCode(digits);
    }

    public static void main(String [] args) throws  NoSuchAlgorithmException, InvalidKeyException {
        String key = "NXV37JUCLJFU7AO5NTCJ23Y5SJKQQIP4VUTCJQDF3K4BVMFWQ7QQ====";
        String digits = "005611";
        System.out.println(loginStep3(key, digits));
    }

}
