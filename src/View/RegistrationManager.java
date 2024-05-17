package View;

import Model.Register;

public class RegistrationManager {
    public static boolean register(boolean isFirstAccess) {
        Register r = new Register();
        while (true) {
            RegistrationForm form = new RegistrationForm(null, isFirstAccess, (certPath, keyPath, secretPhrase, group, password, confirmPassword) -> {
                r.fillInfo(certPath, keyPath, secretPhrase, group, password, confirmPassword);
                r.registerUser();
            });
            form.setLocationRelativeTo(null); // Center the dialog
            form.setVisible(true); // This will block until the dialog is closed
            // Check the registration status
            if (form.isRegistrationSuccessful()) {
                return true; // Registration was successful
            } else if (!form.isVisible()) {
                return false; // Dialog was closed
            }
        }
    }
}
