import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class Register {
    public String pathCertificate;
    public String pathPrivateKey;
    public String secretPhrase;
    public Group group;
    public String password;
    public String confirmPassword;
    public DigitalCertificate digitalCertificate;

    private void validateKey() throws Exception{
        SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
        rand.setSeed(secretPhrase.getBytes());
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, rand);
        Key key = keyGen.generateKey();
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        Path path = Paths.get(this.pathPrivateKey);
        byte[] bytes = Files.readAllBytes(path);

        String chavePrivadaBase64 = new String(cipher.doFinal(bytes), "UTF8");
        chavePrivadaBase64 = chavePrivadaBase64.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").trim();
        chavePrivadaBase64 = chavePrivadaBase64.replaceAll("\\s+", "");

        byte[] chavePrivadaBytes = Base64.getDecoder().decode(chavePrivadaBase64);

        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(chavePrivadaBytes));

        byte[] data = new byte[4096];
        SecureRandom random = new SecureRandom();
        random.nextBytes(data);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedData = md.digest(data);

        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] digitalSignature = cipher.doFinal(hashedData);

        Path certificatePath = Paths.get(this.pathCertificate);
        byte[] certificateBytes = Files.readAllBytes(certificatePath);

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
        PublicKey publicKey = certificate.getPublicKey();

        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedData = cipher.doFinal(digitalSignature);
        boolean isVerified = java.util.Arrays.equals(hashedData, decryptedData);

        if (!isVerified){
            throw new InvalidPrivateKeyException();
        }
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
        this.validateKey();
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
        // Save into the database
        return;
    }

    public boolean validateAdmin(){
        return true;
    }
}
