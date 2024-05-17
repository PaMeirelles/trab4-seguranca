package Model;


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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;

import static Model.DatabaseManager.getAdmLogin;

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
    public String totpKey;

    public void fillInfo(String certPath, String keyPath, String secretPhrase, String group, String password, String confirmPassword) {
        this.pathCertificate = certPath;
        this.pathPrivateKey = keyPath;
        this.secretPhrase = secretPhrase;
        this.group = Group.valueOf(group.toUpperCase());
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
    private byte[] retrievePrivateKey() throws IOException {
        Path path = Paths.get(pathPrivateKey);
        return Files.readAllBytes(path);
    }

    private byte[] trimKey(byte[] keyBytes){
        String chavePrivadaBase64 = new String(keyBytes, StandardCharsets.UTF_8);
        chavePrivadaBase64 = chavePrivadaBase64.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").trim();
        chavePrivadaBase64 = chavePrivadaBase64.replaceAll("\\s+", "");
        return Base64.getDecoder().decode(chavePrivadaBase64);
    }

    public static Key genKey(String seed) throws NoSuchAlgorithmException {
        SecureRandom rand = SecureRandom.getInstance(Constants.SECURE_RANDOM_ALGO);
        rand.setSeed(seed.getBytes());

        KeyGenerator keyGen = KeyGenerator.getInstance(Constants.KEY_GENERATOR_ALGO);
        keyGen.init(Constants.KEY_SIZE, rand);
        return keyGen.generateKey();
    }

    private void fillPrivateKey(byte [] bytes, boolean fromFile) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        Key chave = genKey(secretPhrase);
        Cipher cipher = Cipher.getInstance(Constants.CYPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, chave);
        byte[] chavePrivadaBytes;
        if(fromFile){
            chavePrivadaBytes = trimKey(cipher.doFinal(bytes));
        }
        else{
            chavePrivadaBytes = cipher.doFinal(bytes);
        }

        KeyFactory factory = KeyFactory.getInstance(Constants.KEY_ALGO);
        privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(chavePrivadaBytes));
    }

    private void fillCertificate() throws FileNotFoundException, CertificateException {
        FileInputStream fis = new FileInputStream(this.pathCertificate);
        CertificateFactory certificateFactory = CertificateFactory.getInstance(Constants.CERTIFICATE_TYPE);
        this.certificate = (X509Certificate) certificateFactory.generateCertificate(fis);
    }

    private byte[] genRandomBytes(int numBytes){
        byte[] data = new byte[numBytes];
        SecureRandom random = new SecureRandom();
        random.nextBytes(data);
        return data;
    }

    private boolean validateKey() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] data = genRandomBytes(Constants.TEST_ARRAY_SIZE);

        MessageDigest md = MessageDigest.getInstance(Constants.DIGEST_ALGO);
        byte[] hashedData = md.digest(data);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] digitalSignature = cipher.doFinal(hashedData);

        cipher.init(Cipher.DECRYPT_MODE, certificateInfo.publicKey);
        byte[] decryptedData = cipher.doFinal(digitalSignature);

        return Arrays.equals(hashedData, decryptedData);
    }

    private void checkInfo() throws InvalidPasswordFormatException, LoginNotUniqueException, PasswordMismatchException, RepeatingCharactersException, InvalidPrivateKeyException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (!password.matches("^[0-9]{8,10}$")) {
            throw new InvalidPasswordFormatException();
        }
        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException();
        }

        if (DatabaseManager.loginIsNotUnique(this.certificateInfo.email)){
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
        if(!this.validateKey()){
            throw new InvalidPrivateKeyException();
        }
    }

    private void fillForTest(){
        this.pathCertificate = "D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Keys\\admin-x509.crt";
        this.pathPrivateKey = "D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Keys\\admin-pkcs8-aes.pem";
        this.secretPhrase = "admin";
        this.group = Group.ADMIN;
        this.password = "06052024";
        this.confirmPassword = "06052024";
    }

    private String generateTotpKey() throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
        byte[] randomBytes = genRandomBytes(Constants.TOTP_SIZE);
        Base32 base32Encoder = new Base32(Base32.Alphabet.BASE32, true, false);
        return base32Encoder.toString(randomBytes);
    }

    public void registerUser() throws Exception {
        this.fillCertificate();
        this.certificateInfo = new CertificateInfo(this.certificate);
        this.totpKey = generateTotpKey();
        this.fillPrivateKey(retrievePrivateKey(), true);
        this.checkInfo();
        DatabaseManager.saveUser(totpKey, secretPhrase, certificateInfo.email, this.password, this.privateKey, this.certificate, this.certificateInfo.subjectFriendlyName, this.group);
    }

    public boolean validatesecretPhrase(String login, String secretPhrase) throws Exception {
        X509Certificate cert = DatabaseManager.retrieveCertificate(login);
        byte[] privateKeyBytes = DatabaseManager.retrieveprivateKeyBytes(login);
        this.secretPhrase = secretPhrase;
        this.certificate = cert;
        this.certificateInfo = new CertificateInfo(this.certificate);
        fillPrivateKey(privateKeyBytes, false);
        return validateKey();
    }

    public boolean validateAdmin(String secretPhrase) throws Exception{
        return validatesecretPhrase(getAdmLogin(), secretPhrase);
    }

    public static void main(String[] args) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Register r = new Register();
        r.fillForTest();
        String oi = r.generateTotpKey();
        System.out.println(oi);
    }
}
