import java.security.PrivateKey;
import java.security.cert.Certificate;

public class DatabaseManager {
    public static boolean isFirstAccess(){
        return true;
    }
    public static boolean loginIsUnique(String login){
        return true;
    }

    private static String preparePassword(String password){
        return "";
    }

    private static byte[] preparePrivateKey(PrivateKey privateKey){
        return new byte[0];
    }

    public static void saveUser(String login, String password, PrivateKey privateKey, Certificate certificate, String friendlyName){

    }
}
