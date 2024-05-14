package Model;

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
    private static int getCurrentEpochInterval(){
        // TODO;
        return 0;
    }

    private static String calculateCode(String userKeyBase32, int interval){
        // TODO
        return "";
    }

    public static boolean loginStep3(String userKeyBase32, String digits){
        int currentInterval = getCurrentEpochInterval();
        int pastInterval = getCurrentEpochInterval() - 1;
        int nextInterval = getCurrentEpochInterval() + 1;
        return Objects.equals(calculateCode(userKeyBase32, currentInterval), digits) ||
                Objects.equals(calculateCode(userKeyBase32, pastInterval), digits) ||
                Objects.equals(calculateCode(userKeyBase32, nextInterval), digits);
    }

}
