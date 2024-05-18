package View;

import Model.Register;

import java.sql.SQLException;

public class RegistrationManager {
    public static RegistrationResult register(boolean isFirstAccess, String login) throws SQLException {
        Register r = new Register();
        while (true) {
            RegistrationForm form = new RegistrationForm(login, null, isFirstAccess, (certPath, keyPath, secretPhrase, group, password, confirmPassword) -> {
                r.fillInfo(certPath, keyPath, secretPhrase, group, password, confirmPassword);
                return r.registerUser();
            });
            if (form.isGoBackPressed()) {
                return RegistrationResult.GO_BACK;
            } else if (form.isRegistrationSuccessful()) {
                return RegistrationResult.SUCCESS;
            } else {
                // Close the form if it's neither successful nor "Go Back"
                form.dispose();
            }
        }
    }

    public enum RegistrationResult {
        SUCCESS,
        GO_BACK
    }
}
