package Controller;

import Model.DatabaseManager;
import Model.LoginModel;
import Model.Register;
import View.*;
import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

import static Model.DatabaseManager.log;

public class Main {
    public static String frase_secreta = null;
    public static String login = null;
    public static String password = null;

    public static void main(String[] args) throws Exception {
        preRun();
        run();
    }

    public static void preRun() throws SQLException {
        log("1001");
        boolean isFirstAccess = DatabaseManager.isFirstAccess();

        if (isFirstAccess) {
            while (true) {
                RegistrationManager.RegistrationResult r = RegistrationManager.register(true, login);
                if (r == RegistrationManager.RegistrationResult.SUCCESS) {
                    break;
                }
            }
        }
        startAuthenticationProcess();
    }

    public static void run() throws Exception {
        while (true){
            if (startLoginProcess()) {
                if (startPasswordProcess()){
                    if (startTotpProcess()){
                        break;
                    }
                }
            }
        }
        log("1003", login);
        MainMenu.createAndShowGUI(login, frase_secreta);
    }

    private static void startAuthenticationProcess() throws SQLException {
        frase_secreta = AdminValidation.secretPhraseInput();

        Register r = new Register();
        try {
            if (!r.validateAdmin(frase_secreta)) {
                JOptionPane.showMessageDialog(null, "Frase incorreta. Encerrando o sistema");
                endSystem();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Frase incorreta. Encerrando o sistema");
            endSystem();
        }
    }

    private static boolean startLoginProcess() throws Exception {
        log("2001");
        while (true) {
            Main.login = Login.login();
            boolean loginExists = LoginModel.loginStep1(Main.login);
            if (loginExists) {
                if (DatabaseManager.userIsBlocked(login)) {
                    log("2004", login);
                    JOptionPane.showMessageDialog(null, "Seu acesso esta bloqueado.");
                    return false;
                }
                else {
                    log("2003", login);
                    break;
                }
            }
            else {
                log("2005", login);
                JOptionPane.showMessageDialog(null, "Login nao encontrado. Tente novamente.");
            }
        }
        log("2002");
        return true;
    }

    private static boolean startPasswordProcess() throws Exception {
        log("3001", login);
        int attemptsRemaining = 3;
        List<String> passwords;
        while (attemptsRemaining > 0) {
            passwords = PasswordInput.createAndShowGUI();
            String pass = LoginModel.loginStep2(Main.login, passwords);
            if (pass != null) {
                log("3003", login);
                password = pass;
                return true;

            } else {
                attemptsRemaining -= 1;
                if (attemptsRemaining == 0) {
                    log("3006", login);
                    DatabaseManager.blockUser(login);
                    log("3007", login);
                    JOptionPane.showMessageDialog(null, "Senha incorreta. Seu acesso foi bloqueado por 2 minutos");
                    // Reset and redirect to login screen
                    resetAndRestart();
                    return false;
                } else {
                    if (attemptsRemaining == 2) {
                        log("3004", login);
                    } else {
                        log("3005", login);
                    }
                    JOptionPane.showMessageDialog(null, "Senha incorreta. Tentativas restantes: " + attemptsRemaining);
                }
            }
        }
        log("3002", login);

        return false;
    }

    private static boolean startTotpProcess() throws Exception {
        log("4001", login);
        int attemptsRemaining = 3;
        String totpCode;
        while (attemptsRemaining > 0) {
            totpCode = Login.collectTOTPCode();
            boolean codeCorrect = LoginModel.loginStep3(DatabaseManager.getUserTotpKey(Main.login, password), totpCode);
            if (codeCorrect) {
                log("4003", login);
                password = null;
                return true;

            } else {
                attemptsRemaining -= 1;
                if (attemptsRemaining == 0) {
                    log("4006", login);
                    DatabaseManager.blockUser(login);
                    log("4007", login);
                    JOptionPane.showMessageDialog(null, "Código incorreto. Seu acesso foi bloqueado por 2 minutos");
                    // Reset and redirect to login screen
                    resetAndRestart();
                    return false;
                } else {
                    if(attemptsRemaining == 2){
                        log("4004", login);
                    } else {
                        log("4005", login);
                    }
                    JOptionPane.showMessageDialog(null, "Código incorreto. Tentativas restantes: " + attemptsRemaining);
                }
            }
        }
        log("4002", login);
        return false;
    }

    public static void resetAndRestart() throws Exception {
        log("1004", login);
        login = null;
        password = null;
        run();
    }

    public static void endSystem() throws SQLException {
        log("1002");
        System.exit(0);
    }
}
