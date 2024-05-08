import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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
        SecureRandom rand = SecureRandom.getInstance(Constants.SECURE_RANDOM_ALGO);
        rand.setSeed(secretPhrase.getBytes());

        KeyGenerator keyGen = KeyGenerator.getInstance(Constants.KEY_GENERATOR_ALGO);
        keyGen.init(Constants.KEY_SIZE, rand);
        Key key = keyGen.generateKey();

        Cipher cipher = Cipher.getInstance(Constants.CYPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);

        Path path = Paths.get(this.pathPrivateKey);
        byte[] bytes = Files.readAllBytes(path);

        String privateKeyBase64 = new String(cipher.doFinal(bytes), StandardCharsets.UTF_8);
        privateKeyBase64 = privateKeyBase64.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").trim();
        privateKeyBase64 = privateKeyBase64.replaceAll("\\s+", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);

        KeyFactory factory = KeyFactory.getInstance(Constants.KEY_ALGO);
        PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

        byte[] data = new byte[Constants.TEST_ARRAY_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(data);

        MessageDigest md = MessageDigest.getInstance(Constants.DIGEST_ALGO);
        byte[] hashedData = md.digest(data);

        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] digitalSignature = cipher.doFinal(hashedData);

        Path certificatePath = Paths.get(this.pathCertificate);
        byte[] certificateBytes = Files.readAllBytes(certificatePath);

        CertificateFactory certificateFactory = CertificateFactory.getInstance(Constants.CERTIFICATE_TYPE);
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
        PublicKey publicKey = certificate.getPublicKey();

        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedData = cipher.doFinal(digitalSignature);
        boolean isVerified = java.util.Arrays.equals(hashedData, decryptedData);

        if (!isVerified){
            throw new InvalidPrivateKeyException();
        }
    }

    private void checkInfo() throws Exception {
        if (!password.matches("^[0-9]{8,10}$")) {
            throw new InvalidPasswordFormatException();
        }
        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException();
        }

        char prev = password.charAt(0);
        for (int i = 1; i < password.length(); i++) {
            char curr = password.charAt(i);
            if (curr == prev) {
                throw new RepeatingCharactersException();
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

    public void registerAdmin() throws Exception {
        this.fillForTest();
        this.digitalCertificate = new DigitalCertificate(this.pathCertificate);
        this.checkInfo();
        // Save into the database
    }

    public boolean validateAdmin(){
        return true;
    }
}
