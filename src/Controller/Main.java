package Controller;

import Model.DatabaseManager;
import Model.Register;
import View.AdminValidation;

import javax.swing.*;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) throws Exception {
        boolean isFirstAccess = DatabaseManager.isFirstAccess();
        Register r = new Register();

        if (isFirstAccess) {
            r.registerAdmin();
        } else {
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
}
