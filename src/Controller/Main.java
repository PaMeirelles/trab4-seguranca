package Controller;

import Model.DatabaseManager;
import Model.Register;
import View.AdminValidation;
import View.RegistrationForm;

import javax.swing.*;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) throws Exception {
        boolean isFirstAccess = DatabaseManager.isFirstAccess();
        Register r = new Register();

        if (isFirstAccess) {
            RegistrationForm registrationForm = new RegistrationForm(new RegistrationCallback() {
                @Override
                public void onSubmit(String certPath, String keyPath, String secretPhrase, String group, String password, String confirmPassword) throws Exception {
                    // Process the form data after submission
                    r.fillInfo(certPath, keyPath, secretPhrase, group, password, confirmPassword);
                    r.registerAdmin();
                }
            });
        } else {
            validateAdmin(r);
        }
    }

    public static void validateAdmin(Register r) {
        Consumer<String> submitFunction = new Consumer<String>() {
            @Override
            public void accept(String inputText) {
                try {
                    boolean adminValidated = r.validateAdmin(inputText);
                    if (adminValidated) {
                        JOptionPane.showMessageDialog(null, "Admin validated!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AdminValidation(submitFunction);
            }
        });
    }
}
