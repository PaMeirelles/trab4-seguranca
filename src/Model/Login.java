package Model;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.sql.SQLException;
import java.util.List;


public class Login {
    public String login;
    public List<String> possiblePasswords;
    public boolean loginStep1() throws Exception {
        return DatabaseManager.loginIsNotUnique(login);
    }
    public boolean loginStep2() throws SQLException {
        String dbPass = DatabaseManager.retrievePassword(login);
        for(String password: possiblePasswords){
            if (OpenBSDBCrypt.checkPassword(password, dbPass.getBytes())){
                return true;
            }
        }
        return false;
    }
    public void login() throws Exception {
        if(!loginStep1()){
            // TODO
            return;
        }
        if(!loginStep2()){
            // TODO
            return;
        }
        return;
    }

}
