package Model;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.sql.*;
import java.util.Base64;

public class DatabaseManager {
    public static boolean isFirstAccess() throws SQLException {
        return true;
        /*
        Connection conn = getConnection();
        String query = "SELECT COUNT(*) AS count FROM usuarios";
        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            int count = resultSet.getInt("count");
            conn.close();
            return count == 0;
        }
        conn.close();
        return true;
    */}
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(Constants.CONNECTION_STRING);
    }
    public static boolean loginIsNotUnique(String login) throws SQLException {
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

    public static void blockUser(String login) throws SQLException {
        long currentTime = System.currentTimeMillis();
        long blockedUntil = currentTime + Constants.BLOCK_TIME;
        String query = "UPDATE usuarios SET blocked_until = ? WHERE login = ?";
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, blockedUntil);
        statement.setString(2, login);
        statement.executeUpdate(); // Use executeUpdate() instead of executeQuery()
        connection.close();
    }

    public static boolean userIsBlocked(String login) throws SQLException {
        String query = "SELECT blocked_until FROM usuarios WHERE login = ?";
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, login);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) { // Move the cursor to the first row
            long blockedUntil = resultSet.getLong("blocked_until");
            if (resultSet.wasNull()) { // Check if the value was NULL
                connection.close();
                return false; // If it was NULL, return false
            }
            long currentTime = System.currentTimeMillis();
            connection.close();
            return blockedUntil > currentTime; // Check if blockedUntil is in the future
        }
        connection.close();
        return false; // Return false if no rows were found for the given login
    }


    public static byte[] retrieveprivateKeyBytes(String login) throws SQLException {
        String query = "SELECT private_key FROM KeyByLogin WHERE login = ?";
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, login);

        ResultSet resultSet = statement.executeQuery();
        byte[] bytes = resultSet.getBytes("private_key");
        connection.close();

        return bytes;
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

    private static String prepareTotpKey(String password, String totpKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key chave = Register.genKey(password);
        Cipher cipher = Cipher.getInstance(Constants.CYPHER_TRANSFORMATION);;
        cipher.init(Cipher.ENCRYPT_MODE, chave);
        byte[] encryptedBytes = cipher.doFinal(totpKey.getBytes());
        Base32 base32Encoder = new Base32(Base32.Alphabet.BASE32, true, false);
        return base32Encoder.toString(encryptedBytes);
    }

    private static int getGroupId(Group group){
        switch (group) {
            case ADMIN:
                return 1;
            case USER:
                return 2;
            default:
                return -1;
        }
    }

    private static int saveKeys(String totpKey, String secretPhrase, PrivateKey privateKey, Certificate certificate, Connection connection, String password) throws SQLException, CertificateEncodingException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String insertSQL = "INSERT INTO chaveiro (digital_certificate, private_key, totp_key) VALUES (?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(insertSQL);
        String encodedCertificate = Base64.getEncoder().encodeToString(certificate.getEncoded());

        statement.setString(3, prepareTotpKey(password, totpKey));
        statement.setBytes(2, preparePrivateKey(privateKey, secretPhrase));
        statement.setString(1, encodedCertificate);

        statement.executeUpdate();

        int generatedKid = -1; // Default value if no key was generated
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            generatedKid = generatedKeys.getInt(1);
        }
        connection.close();
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
        connection.close();
    }

    public static void saveUser(String totpKey, String secretPhrase, String login, String password, PrivateKey privateKey, Certificate certificate, String friendlyName, Group group) throws SQLException, CertificateEncodingException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Connection connection = getConnection();
        int kid = saveKeys(totpKey, secretPhrase, privateKey, certificate, connection, password);
        String preparedPassword = preparePassword(password);
        saveUser(kid, login, preparedPassword, friendlyName, group, connection);
        connection.close();
    }

    public static String getAdmLogin(){
        // TODO
        return "admin@inf1416.puc-rio.br";
    }

    public static String getUserTotpKey(String login){
        // TODO
        return "NXV37JUCLJFU7AO5NTCJ23Y5SJKQQIP4VUTCJQDF3K4BVMFWQ7QQ====";
    }

    public static void main(String[] args) throws Exception{
        String login = "admin@inf1416.puc-rio.br";
        boolean userIsLocked = userIsBlocked(login);
        System.out.println(userIsLocked);
    }
}
