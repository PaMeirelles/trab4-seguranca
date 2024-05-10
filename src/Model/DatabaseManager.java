package Model;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.io.*;
import java.security.Key;
import java.security.SecureRandom;
import java.security.cert.*;
import java.sql.*;
import java.util.Base64;

public class DatabaseManager {
    public static boolean isFirstAccess(){
        // TODO
        return true;
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

    public static String retrievePassword(String login) throws SQLException {
        String query = "SELECT password FROM usuarios WHERE login = ?";
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, login);

        ResultSet resultSet = statement.executeQuery();
        connection.close();
        return resultSet.getNString("password");

    }

    public static X509Certificate retrieveCertificate(String login) throws SQLException, CertificateException {
        String query = "SELECT certificate FROM usuarios WHERE login = ?";
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, login);

        ResultSet resultSet = statement.executeQuery();
        connection.close();
        CertificateFactory certificateFactory = CertificateFactory.getInstance(Constants.CERTIFICATE_TYPE);
        byte[] certBytes = resultSet.getBytes(1);
        ByteArrayInputStream fis = new ByteArrayInputStream(certBytes);

        return (X509Certificate) certificateFactory.generateCertificate(fis);

    }
    private static byte[] preparePrivateKey(Key privateKey) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(privateKey);
        objectOutputStream.flush();
        objectOutputStream.close();

        return byteArrayOutputStream.toByteArray();
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

    private static int saveKeys(Key privateKey, Certificate certificate, Connection connection) throws SQLException, CertificateEncodingException, IOException {
        String insertSQL = "INSERT INTO chaveiro (digital_certificate, private_key) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(insertSQL);
        String encodedCertificate = Base64.getEncoder().encodeToString(certificate.getEncoded());

        statement.setBytes(2, preparePrivateKey(privateKey));
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

    public static void saveUser(String login, String password, Key privateKey, Certificate certificate, String friendlyName, Group group) throws SQLException, CertificateEncodingException, IOException {
        Connection connection = getConnection();
        int kid = saveKeys(privateKey, certificate, connection);
        String preparedPassword = preparePassword(password);
        saveUser(kid, login, preparedPassword, friendlyName, group, connection);
        connection.close();
    }

    public static String getAdmLogin(){
        return "admin";
    }
}
