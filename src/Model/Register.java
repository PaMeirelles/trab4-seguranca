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
    private byte[] retrievePrivateKey() throws InvalidPrivateKeyException {
        try{
            Path path = Paths.get(pathPrivateKey);
            return Files.readAllBytes(path);
        }
        catch (IOException ex){
            throw new InvalidPrivateKeyException(InvalidPrivateKeyException.InvalidKeyType.INVALID_PATH);
        }
    }

    private static byte[] trimKey(byte[] keyBytes){
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

    public static PrivateKey genPrivateKey(byte[] privateKeyBytes, boolean fromFile, String secretPhrase) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        Key chave = genKey(secretPhrase);
        Cipher cipher = Cipher.getInstance(Constants.AES_CYPHER);
        cipher.init(Cipher.DECRYPT_MODE, chave);
        byte[] chavePrivadaBytes;
        if (fromFile) {
            chavePrivadaBytes = trimKey(cipher.doFinal(privateKeyBytes));
        } else {
            chavePrivadaBytes = cipher.doFinal(privateKeyBytes);
        }

        KeyFactory factory = KeyFactory.getInstance(Constants.KEY_ALGO);
        return factory.generatePrivate(new PKCS8EncodedKeySpec(chavePrivadaBytes));
    }

    private void fillPrivateKey(byte [] bytes, boolean fromFile) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, InvalidKeySpecException, InvalidPrivateKeyException {
        try {
            privateKey = genPrivateKey(bytes, fromFile, secretPhrase);
        }
        catch (BadPaddingException ex){
            throw new InvalidPrivateKeyException(InvalidPrivateKeyException.InvalidKeyType.INVALID_SECRET_PHRASE);
        }
    }

    private void fillCertificate() throws CertificateException {
        try {
            FileInputStream fis = new FileInputStream(this.pathCertificate);
            CertificateFactory certificateFactory = CertificateFactory.getInstance(Constants.CERTIFICATE_TYPE);
            this.certificate = (X509Certificate) certificateFactory.generateCertificate(fis);
        }
        catch (FileNotFoundException e){
            throw new CertificateException();
        }

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

        Cipher cipher = Cipher.getInstance(Constants.RSA_CYPHER);
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
            throw new InvalidPrivateKeyException(InvalidPrivateKeyException.InvalidKeyType.INVALID_DIGITAL_SIGNATURE);
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

    private String generateTotpKey() {
        byte[] randomBytes = genRandomBytes(Constants.TOTP_SIZE);
        Base32 base32Encoder = new Base32(Base32.Alphabet.BASE32, true, false);
        return base32Encoder.toString(randomBytes);
    }

    public String registerUser() throws InvalidPrivateKeyException, PasswordMismatchException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, InvalidPasswordFormatException, RepeatingCharactersException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, LoginNotUniqueException, InvalidKeySpecException, CertificateException {
        this.fillCertificate();
        this.certificateInfo = new CertificateInfo(this.certificate);
        this.totpKey = generateTotpKey();
        this.fillPrivateKey(retrievePrivateKey(), true);
        this.checkInfo();
        DatabaseManager.saveUser(totpKey, secretPhrase, certificateInfo.email, this.password, this.privateKey, this.certificate, this.certificateInfo.subjectFriendlyName, this.group);
        return totpKey;
    }

    public boolean validateSecretPhrase(String login, String secretPhrase) throws SQLException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidPrivateKeyException {
        X509Certificate cert = DatabaseManager.retrieveCertificate(login);
        byte[] privateKeyBytes = DatabaseManager.retrieveprivateKeyBytes(login);
        this.secretPhrase = secretPhrase;
        this.certificate = cert;
        this.certificateInfo = new CertificateInfo(this.certificate);
        fillPrivateKey(privateKeyBytes, false);
        return validateKey();
    }

    public boolean validateAdmin(String secretPhrase) throws Exception{
        return validateSecretPhrase(getAdmLogin(), secretPhrase);
    }

    public static void main(String[] args) throws Exception {
        Register r = new Register();
        r.fillForTest();
        r.registerUser();
        String oi = r.generateTotpKey();
        System.out.println(oi);
    }
}
