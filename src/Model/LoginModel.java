package Model;

import java.sql.SQLException;
import java.util.List;


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

}
