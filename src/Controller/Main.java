package Controller;

import Model.DatabaseManager;
import Model.LoginModel;
import Model.Register;
import View.*;

import javax.swing.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static Model.DatabaseManager.log;
import Model.Group;



public class Main {
    public static String frase_secreta = null;
    public static String login = null;
    public static String password = null;
    public static void main(String[] args) throws SQLException {
        log("1001");
        boolean isFirstAccess = DatabaseManager.isFirstAccess();
        
        //TODO: Remover testes das telas
        //RegistrationManager.register(true, login);
        //MainMenu.createAndShowGUI(login, frase_secreta);
        // ExitScreen.createAndShowGUI(login, Group.ADMIN);
        //MainMenu.createAndShowGUI("ca@grad.inf.puc-rio.br", "admin");

        if (isFirstAccess) {
            while(isFirstAccess){
                RegistrationManager.RegistrationResult r = RegistrationManager.register(true, login);
                if(r == RegistrationManager.RegistrationResult.SUCCESS){
                    break;
                }
            }
        }
        startAuthenticationProcess();
        startLoginProcess();
        startPasswordProcess();
        startTotpProcess();
        log("1003", login);
        MainMenu.createAndShowGUI(login, frase_secreta);
    }
    private static void startAuthenticationProcess() throws SQLException {
        frase_secreta = AdminValidation.secretPhraseInput();

        Register r = new Register();
        try {
            if (!r.validateAdmin(frase_secreta)){
                JOptionPane.showMessageDialog(null, "Frase incorreta. Encerrando o sistema");
                Main.endSystem();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Frase incorreta. Encerrando o sistema");
            Main.endSystem();
        }
    }

    private static void startLoginProcess() throws SQLException {
        log("2001");
        try {
            while (true) {
                Main.login = Login.login();
                boolean loginExists = LoginModel.loginStep1(Main.login);
                if (loginExists) {
                    if(DatabaseManager.userIsBlocked(login)){
                        log("2004", login);
                        JOptionPane.showMessageDialog(null, "Seu acesso está bloqueado.");
                    }
                    else {
                        log("2003", login);
                        break;
                    }
                } else {
                    log("2005", login);
                    JOptionPane.showMessageDialog(null, "Login não encontrado. Tente novamente.");
                }
            }
            log("2002");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void startPasswordProcess() {
        try {
            log("3001", login);
            int attemptsRemaining = 3;
            List<String> passwords = Collections.emptyList();
            while (attemptsRemaining > 0) {
                passwords = Login.collectPassword();
                String pass = LoginModel.loginStep2(Main.login, passwords);
                if (pass != null) {
                    log("3003", login);
                    password = pass;
                    break;
                } else {
                    attemptsRemaining -= 1;
                    if (attemptsRemaining == 0){
                        log("3006", login);
                        DatabaseManager.blockUser(login);
                        log("3007", login);
                        JOptionPane.showMessageDialog(null, "Senha incorreta. Seu acesso foi bloqueado por 2 minutos");
                        // TODO: Redirecionar para a tela de login invés disso
                        endSystem();
                    }
                    else {
                        if(attemptsRemaining == 2){
                            log("3004", login);
                        }
                        else{
                            log("3005", login);
                        }
                        JOptionPane.showMessageDialog(null, "Senha incorreta. Tentativas restantes: " + attemptsRemaining);
                    }
                }
            }
            log("3002", login);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startTotpProcess() throws SQLException {
        log("4001", login);
        try {
            int attemptsRemaining = 3;
            String totpCode;
            while (attemptsRemaining > 0) {
                totpCode = Login.collectTOTPCode();
                boolean codeCorrect = LoginModel.loginStep3(DatabaseManager.getUserTotpKey(Main.login, password), totpCode);
                if(codeCorrect){
                    log("4003", login);
                    password = null;
                    break;
                }
                else {
                    attemptsRemaining -= 1;
                    if (attemptsRemaining == 0) {
                        log("4006", login);
                        DatabaseManager.blockUser(login);
                        log("4007", login);
                        JOptionPane.showMessageDialog(null, "Código incorreto. Seu acesso foi bloqueado por 2 minutos");
                        // TODO: Redirecionar para a tela de login
                        endSystem();
                    } else {
                        if(attemptsRemaining == 2){
                            log("4004", login);
                        }
                        else{
                            log("4005", login);
                        }
                        JOptionPane.showMessageDialog(null, "Código incorreto. Tentativas restantes: " + attemptsRemaining);
                    }
                }
            }
            log("4002", login);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void endSystem() throws SQLException {
        log("1002");
        System.exit(0);
    }
}
