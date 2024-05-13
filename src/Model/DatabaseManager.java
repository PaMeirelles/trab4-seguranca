package Model;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.*;
import java.util.Base64;

public class DatabaseManager {
    public static boolean isFirstAccess(){
        // TODO
        return false;
    }
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(Constants.CONNECTION_STRING);
    }
    public static boolean loginIsNotUnique(String login) throws Exception {
        String query = "SELECT COUNT(*) AS count FROM usuarios WHERE login = ?";
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, login);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            int count = resultSet.getInt("count");
            connection.close();
            return count != 0;
        }
        connection.close();
        return true;
    }
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[Constants.SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    private static String preparePassword(String password){
        return OpenBSDBCrypt.generate(password.getBytes(), generateSalt(), Constants.COST_FACTOR);
  }

  public static boolean checkPassword(String pass, String dbPass){
        return OpenBSDBCrypt.checkPassword(dbPass, pass.getBytes());
  }

    public static String retrievePassword(String login) throws SQLException {
        String query = "SELECT password FROM usuarios WHERE login = ?";
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, login);

        ResultSet resultSet = statement.executeQuery();
        String password = resultSet.getString("password");
        connection.close();
        return password;

    }

    public static X509Certificate retrieveCertificate(String login) throws SQLException, CertificateException {
        String query = "SELECT digital_certificate FROM KeyByLogin WHERE login = ?";
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, login);

        ResultSet resultSet = statement.executeQuery();
        CertificateFactory certificateFactory = CertificateFactory.getInstance(Constants.CERTIFICATE_TYPE);
        byte[] certBytes = Base64.getDecoder().decode(resultSet.getString("digital_certificate"));
        ByteArrayInputStream fis = new ByteArrayInputStream(certBytes);
        connection.close();
        return (X509Certificate) certificateFactory.generateCertificate(fis);

    }

    public static byte[] retrieveprivateKeyBytes(String login) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String query = "SELECT private_key FROM KeyByLogin WHERE login = ?";
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, login);

        ResultSet resultSet = statement.executeQuery();

        return resultSet.getBytes("private_key");
    }
    private static byte[] preparePrivateKey(PrivateKey privateKey, String secretPhrase) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecureRandom rand = SecureRandom.getInstance(Constants.SECURE_RANDOM_ALGO);
        rand.setSeed(secretPhrase.getBytes());

        KeyGenerator keyGen = KeyGenerator.getInstance(Constants.KEY_GENERATOR_ALGO);
        keyGen.init(Constants.KEY_SIZE, rand);
        Key chave = keyGen.generateKey();
        Cipher cipher = Cipher.getInstance(Constants.CYPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, chave);
        return cipher.doFinal(privateKey.getEncoded());
    }

    private static int getGroupId(Group group){
        switch (group) {
            case ADMIN:
                return 1;
            case USER:
                return 2;
            default:
                return -1;
        }    }

    private static int saveKeys(String secretPhrase, PrivateKey privateKey, Certificate certificate, Connection connection) throws SQLException, CertificateEncodingException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String insertSQL = "INSERT INTO chaveiro (digital_certificate, private_key) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(insertSQL);
        String encodedCertificate = Base64.getEncoder().encodeToString(certificate.getEncoded());

        statement.setBytes(2, preparePrivateKey(privateKey, secretPhrase));
        statement.setString(1, encodedCertificate);

        statement.executeUpdate();

        int generatedKid = -1; // Default value if no key was generated
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            generatedKid = generatedKeys.getInt(1);
        }

        return generatedKid;
    }

    private static void saveUser(int kid, String login, String password, String friendlyName, Group group, Connection connection) throws SQLException {
        String insertSQL = "INSERT INTO usuarios (key_id, group_id, login, password, friendly_name) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(insertSQL);

        statement.setInt(1, kid);
        statement.setInt(2, getGroupId(group));
        statement.setString(3, login);
        statement.setString(4, password);
        statement.setString(5, friendlyName);

        statement.executeUpdate();
    }

    public static void saveUser(String secretPhrase, String login, String password, PrivateKey privateKey, Certificate certificate, String friendlyName, Group group) throws SQLException, CertificateEncodingException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Connection connection = getConnection();
        int kid = saveKeys(secretPhrase, privateKey, certificate, connection);
        String preparedPassword = preparePassword(password);
        saveUser(kid, login, preparedPassword, friendlyName, group, connection);
        connection.close();
    }

    public static String getAdmLogin(){
        // TODO
        return "admin@inf1416.puc-rio.br";
    }
    public static void main(String[] args){
        String pass = "06052024";
        String hashed = "$2y$12$KqTNxqa/2v5rN83jq6DBWekOnaG5ufjw3H81mbbuKVwNoxfo410TO";
        boolean check = checkPassword(pass, hashed);
        System.out.println(check);
    }
}
