package Controller;

import Model.DatabaseManager;
import Model.LoginModel;
import Model.Register;
import View.AdminValidation;
import View.Login;
import View.RegistrationForm;
import javax.swing.*;
import java.util.List;

public class Main {
    public static String frase_secreta = null;
    public static String login = null;
    public static void main(String[] args) throws Exception {
        boolean isFirstAccess = DatabaseManager.isFirstAccess();
        Register r = new Register();

        if (isFirstAccess) {
            new RegistrationForm((certPath, keyPath, secretPhrase, group, password, confirmPassword) -> {
                r.fillInfo(certPath, keyPath, secretPhrase, group, password, confirmPassword);
                r.registerAdmin();
            });
        } else {
            startAuthenticationProcess();
            startLoginProcess();
            startPasswordProcess();

        }
    }
    private static void startAuthenticationProcess() {
        try {
            while (true) {
                if (frase_secreta == null) {
                    frase_secreta = AdminValidation.secretPhraseInput();
                }
                Register r = new Register();
                if (r.validateAdmin(frase_secreta)){
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String startLoginProcess() {
        try {
            String login;
            while (true) {
                Main.login = Login.login();
                boolean loginExists = LoginModel.loginStep1(Main.login);
                if (loginExists) {
                    System.out.println("Login aceito: " + Main.login);
                    break;
                } else {
                    JOptionPane.showMessageDialog(null, "Login não encontrado. Tente novamente.");
                }
            }
            return Main.login;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static void startPasswordProcess() {
        try {
            List<String> passwords = null;
            while (true) {
                passwords = Login.collectPassword();
                boolean passwordCorrect = LoginModel.loginStep2(Main.login, passwords);
                if (passwordCorrect) {
                    break;
                } else {
                    JOptionPane.showMessageDialog(null, "Senha incorreta. Tente novamente.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
