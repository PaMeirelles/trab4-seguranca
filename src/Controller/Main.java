package Controller;

import Model.DatabaseManager;
import Model.Register;
import View.AdminValidation;
import View.RegistrationForm;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class Main {
    public static String frase_secreta = null;
    public static boolean adminValid;
    public static boolean login1ok;

    public static void main(String[] args) throws Exception {
        boolean isFirstAccess = DatabaseManager.isFirstAccess();
        Register r = new Register();

        if (isFirstAccess) {
            RegistrationForm registrationForm = new RegistrationForm(new RegistrationCallback() {
                @Override
                public void onSubmit(String certPath, String keyPath, String secretPhrase, String group, String password, String confirmPassword) throws Exception {
                    r.fillInfo(certPath, keyPath, secretPhrase, group, password, confirmPassword);
                    r.registerAdmin();
                }
            });
        } else {
            startAuthenticationProcess();
            if(adminValid){
                login();
            }
            else{
                return;
            }
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
    public static void login() throws InterruptedException, InvocationTargetException {
        Consumer<String> submitFunction = new Consumer<String>() {
            @Override
            public void accept(String inputText) {
                try {
                    login1ok = DatabaseManager.loginIsNotUnique(inputText);
                    if(!login1ok){
                        JOptionPane.showMessageDialog(null, "Usuário não cadastrado");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
