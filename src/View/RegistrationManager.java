package View;

import Model.Register;

public class RegistrationManager {
    public static RegistrationResult register(boolean isFirstAccess) {
        Register r = new Register();
        while (true) {
            RegistrationForm form = new RegistrationForm(null, isFirstAccess, (certPath, keyPath, secretPhrase, group, password, confirmPassword) -> {
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
