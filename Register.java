public class Register {
    public String pathCertificate;
    public String pathPrivateKey;
    public String secretPhrase;
    public Group group;
    public String password;
    public String confirmPassword;
    public DigitalCertificate digitalCertificate;

    private void checkInfo() throws InvalidPasswordFormatException, PasswordMismatchException, RepeatingCharactersException {
        if (!password.matches("^[0-9]{8,10}$")) {
            throw new InvalidPasswordFormatException("Invalid password format: Password must be 8-10 digits long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match.");
        }

        char prev = password.charAt(0);
        for (int i = 1; i < password.length(); i++) {
            char curr = password.charAt(i);
            if (curr == prev) {
                throw new RepeatingCharactersException("Password contains consecutive repeating characters.");
            }
        }


    }

    private void fillForTest(){
        this.pathCertificate = "D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Keys\\admin-x509.crt";
        this.pathPrivateKey = "D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Keys\\admin-pkcs8-aes.pem";
        this.secretPhrase = "admin";
        this.group = Group.ADMIN;
        this.password = "05062024";
        this.confirmPassword = "05062024";
    }

    public void registerAdmin() throws Exception {
        this.fillForTest();
        this.digitalCertificate = new DigitalCertificate(this.pathCertificate);
        return;
    }
    public boolean validateAdmin(){
        return true;
    }
}
