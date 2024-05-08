import javax.crypto.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class Register {
    public String pathCertificate;
    public String pathPrivateKey;
    public String secretPhrase;
    public Group group;
    public String password;
    public String confirmPassword;
    public CertificateInfo certificateInfo;
    public PrivateKey privateKey;
    public X509Certificate certificate;

    private void fillPrivateKey() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        SecureRandom rand = SecureRandom.getInstance(Constants.SECURE_RANDOM_ALGO);
        rand.setSeed(secretPhrase.getBytes());

        KeyGenerator keyGen = KeyGenerator.getInstance(Constants.KEY_GENERATOR_ALGO);
        keyGen.init(Constants.KEY_SIZE, rand);
        Key chave = keyGen.generateKey();
        Cipher cipher = Cipher.getInstance(Constants.CYPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, chave);
        Path path = Paths.get(pathPrivateKey);
        byte[] bytes = Files.readAllBytes(path);

        String chavePrivadaBase64 = new String(cipher.doFinal(bytes), StandardCharsets.UTF_8);
        chavePrivadaBase64 = chavePrivadaBase64.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").trim();
        chavePrivadaBase64 = chavePrivadaBase64.replaceAll("\\s+", "");
        byte[] chavePrivadaBytes = Base64.getDecoder().decode(chavePrivadaBase64);

        KeyFactory factory = KeyFactory.getInstance(Constants.KEY_ALGO);
        privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(chavePrivadaBytes));
    }

    private void fillCertificate() throws FileNotFoundException, CertificateException {
        FileInputStream fis = new FileInputStream(this.pathCertificate);
        CertificateFactory certificateFactory = CertificateFactory.getInstance(Constants.CERTIFICATE_TYPE);
        this.certificate = (X509Certificate) certificateFactory.generateCertificate(fis);
    }

    private void validateKey() throws Exception{
        byte[] data = new byte[Constants.TEST_ARRAY_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(data);

        MessageDigest md = MessageDigest.getInstance(Constants.DIGEST_ALGO);
        byte[] hashedData = md.digest(data);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] digitalSignature = cipher.doFinal(hashedData);

        cipher.init(Cipher.DECRYPT_MODE, certificateInfo.publicKey);
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

        if (!DatabaseManager.loginIsUnique(this.certificateInfo.email)){
            throw new LoginNotUniqueException();
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
        this.fillCertificate();
        this.certificateInfo = new CertificateInfo(this.certificate);
        this.fillPrivateKey();
        this.checkInfo();
        DatabaseManager.saveUser(this.certificateInfo.email, this.password, this.privateKey, this.certificate, this.certificateInfo.subjectFriendlyName, this.group);
    }

    public boolean validateAdmin(){
        return true;
    }
}
