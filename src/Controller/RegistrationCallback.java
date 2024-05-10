package Controller;

public interface RegistrationCallback {
    void onSubmit(String certPath, String keyPath, String secretPhrase, String group, String password, String confirmPassword) throws Exception;
}
