import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

public class Register {
    public String pathCertificate;
    public String pathPrivateKey;
    public String secretPhrase;
    public Group group;
    public String password;
    public String confirmPassword;
    public DigitalCertificate digitalCertificate;

    private void validatePublicKey() throws Exception {
        // Read the private key from the PEM file
        FileInputStream privateKeyFile = new FileInputStream(this.pathPrivateKey);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certFactory.generateCertificate(privateKeyFile);
        privateKeyFile.close();
    }

    private void checkInfo() throws Exception, RepeatingCharactersException {
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
            prev = curr;
        }
        this.validatePublicKey();
    }

    private void fillForTest(){
        this.pathCertificate = "D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Keys\\admin-x509.crt";
        this.pathPrivateKey = "D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Keys\\admin-pkcs8-aes.pem";
        this.secretPhrase = "admin";
        this.group = Group.ADMIN;
        this.password = "05062024";
        this.confirmPassword = "05062024";
    }

    public void registerAdmin() throws Exception, RepeatingCharactersException {
        this.fillForTest();
        this.digitalCertificate = new DigitalCertificate(this.pathCertificate);
        this.checkInfo();
        return;
    }

    public boolean validateAdmin(){
        return true;
    }
}
