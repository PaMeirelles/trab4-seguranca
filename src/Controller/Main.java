package Controller;

import Model.DatabaseManager;
import Model.LoginModel;
import Model.Register;
import View.AdminValidation;
import View.Login;
import View.MainMenu;
import View.RegistrationForm;
import javax.swing.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class Main {
    public static String frase_secreta = null;
    public static String login = null;
    public static void main(String[] args) throws SQLException {
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
            startTotpProcess();
            MainMenu.createAndShowGUI();
        }
    }
    private static void startAuthenticationProcess() {
        frase_secreta = AdminValidation.secretPhraseInput();

        Register r = new Register();
        try {
            if (!r.validateAdmin(frase_secreta)){
                JOptionPane.showMessageDialog(null, "Frase incorreta. Encerrando o sistema");
                System.exit(1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Frase incorreta. Encerrando o sistema");
            System.exit(1);
        }
    }

    private static void startLoginProcess() {
        try {
            while (true) {
                Main.login = Login.login();
                boolean loginExists = LoginModel.loginStep1(Main.login);
                if (loginExists) {
                    if(DatabaseManager.userIsBlocked(login)){
                        JOptionPane.showMessageDialog(null, "Seu acesso está bloqueado.");
                    }
                    else {
                        break;
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Login não encontrado. Tente novamente.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void startPasswordProcess() {
        try {
            int attemptsRemaining = 3;
            List<String> passwords = Collections.emptyList();
            while (attemptsRemaining > 0) {
                passwords = Login.collectPassword();
                boolean passwordCorrect = LoginModel.loginStep2(Main.login, passwords);
                if (passwordCorrect) {
                    break;
                } else {
                    attemptsRemaining -= 1;
                    if (attemptsRemaining == 0){
                        DatabaseManager.blockUser(login);
                        // TODO: Redirecionar para a tela de login
                        JOptionPane.showMessageDialog(null, "Senha incorreta. Seu acesso foi bloqueado por 2 minutos");
                        System.exit(0);
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Senha incorreta. Tentativas restantes: " + attemptsRemaining);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startTotpProcess() {
        try {
            int attemptsRemaining = 3;
            String totpCode;
            while (attemptsRemaining > 0) {
                totpCode = Login.collectTOTPCode();
                boolean codeCorrect = LoginModel.loginStep3(DatabaseManager.getUserTotpKey(Main.login), totpCode);
                if(codeCorrect){
                    break;
                }
                else {
                    attemptsRemaining -= 1;
                    if (attemptsRemaining == 0) {
                        DatabaseManager.blockUser(login);
                        // TODO: Redirecionar para a tela de login
                        JOptionPane.showMessageDialog(null, "Código incorreto. Seu acesso foi bloqueado por 2 minutos");
                        System.exit(0);
                    } else {
                        JOptionPane.showMessageDialog(null, "Código incorreto. Tentativas restantes: " + attemptsRemaining);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
