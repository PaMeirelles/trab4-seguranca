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
    }
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
        Cipher cipher = Cipher.getInstance(Constants.AES_CYPHER);
        cipher.init(Cipher.ENCRYPT_MODE, chave);
        return cipher.doFinal(privateKey.getEncoded());
    }

    private static String prepareTotpKey(String password, String totpKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key chave = Register.genKey(password);
        Cipher cipher = Cipher.getInstance(Constants.AES_CYPHER);;
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

    public static String getUserTotpKey(String login) throws SQLException {
        Connection conn = getConnection();
        String query = "SELECT totp_key FROM KeyByLogin WHERE login = ?";
        PreparedStatement statement = conn.prepareStatement(query);

        statement.setString(1, login);
        ResultSet resultSet = statement.executeQuery();
        String code = resultSet.getString("totp_key");
        conn.close();

        return code;
    }

    private static void fillMsgTable() throws SQLException {
        Connection conn = getConnection();
        String[] mensagens = {
                "1001 Sistema iniciado.",
                "1002 Sistema encerrado.",
                "1003 Sessão iniciada para <login_name>.",
                "1004 Sessão encerrada para <login_name>.",
                "2001 Autenticação etapa 1 iniciada.",
                "2002 Autenticação etapa 1 encerrada.",
                "2003 Login name <login_name> identificado com acesso liberado.",
                "2004 Login name <login_name> identificado com acesso bloqueado.",
                "2005 Login name <login_name> não identificado.",
                "3001 Autenticação etapa 2 iniciada para <login_name>.",
                "3002 Autenticação etapa 2 encerrada para <login_name>.",
                "3003 Senha pessoal verificada positivamente para <login_name>.",
                "3004 Primeiro erro da senha pessoal contabilizado para <login_name>.",
                "3005 Segundo erro da senha pessoal contabilizado para <login_name>.",
                "3006 Terceiro erro da senha pessoal contabilizado para <login_name>.",
                "3007 Acesso do usuario <login_name> bloqueado pela autenticação etapa 2.",
                "4001 Autenticação etapa 3 iniciada para <login_name>.",
                "4002 Autenticação etapa 3 encerrada para <login_name>.",
                "4003 Token verificado positivamente para <login_name>.",
                "4004 Primeiro erro de token contabilizado para <login_name>.",
                "4005 Segundo erro de token contabilizado para <login_name>.",
                "4006 Terceiro erro de token contabilizado para <login_name>.",
                "4007 Acesso do usuario <login_name> bloqueado pela autenticação etapa 3.",
                "5001 Tela principal apresentada para <login_name>.",
                "5002 Opção 1 do menu principal selecionada por <login_name>.",
                "5003 Opção 2 do menu principal selecionada por <login_name>.",
                "5004 Opção 3 do menu principal selecionada por <login_name>.",
                "6001 Tela de cadastro apresentada para <login_name>.",
                "6002 Botão cadastrar pressionado por <login_name>.",
                "6003 Senha pessoal inválida fornecida por <login_name>.",
                "6004 Caminho do certificado digital inválido fornecido por <login_name>.",
                "6005 Chave privada verificada negativamente para <login_name> (caminho inválido).",
                "6006 Chave privada verificada negativamente para <login_name> (frase secreta inválida).",
                "6007 Chave privada verificada negativamente para <login_name> (assinatura digital inválida).",
                "6008 Confirmação de dados aceita por <login_name>.",
                "6009 Confirmação de dados rejeitada por <login_name>.",
                "6010 Botão voltar de cadastro para o menu principal pressionado por <login_name>.",
                "7001 Tela de consulta de arquivos secretos apresentada para <login_name>.",
                "7002 Botão voltar de consulta para o menu principal pressionado por <login_name>.",
                "7003 Botão Listar de consulta pressionado por <login_name>.",
                "7004 Caminho de pasta inválido fornecido por <login_name>.",
                "7005 Arquivo de índice decriptado com sucesso para <login_name>.",
                "7006 Arquivo de índice verificado (integridade e autenticidade) com sucesso para <login_name>.",
                "7007 Falha na decriptação do arquivo de índice para <login_name>.",
                "7008 Falha na verificação (integridade e autenticidade) do arquivo de índice para <login_name>.",
                "7009 Lista de arquivos presentes no índice apresentada para <login_name>.",
                "7010 Arquivo <arq_name> selecionado por <login_name> para decriptação.",
                "7011 Acesso permitido ao arquivo <arq_name> para <login_name>.",
                "7012 Acesso negado ao arquivo <arq_name> para <login_name>.",
                "7013 Arquivo <arq_name> decriptado com sucesso para <login_name>.",
                "7014 Arquivo <arq_name> verificado (integridade e autenticidade) com sucesso para <login_name>.",
                "7015 Falha na decriptação do arquivo <arq_name> para <login_name>.",
                "7016 Falha na verificação (integridade e autenticidade) do arquivo <arq_name> para <login_name>.",
                "8001 Tela de saída apresentada para <login_name>.",
                "8002 Botão encerrar sessão pressionado por <login_name>.",
                "8003 Botão encerrar sistema pressionado por <login_name>.",
                "8004 Botão voltar de sair para o menu principal pressionado por <login_name>."
        };
        if (conn != null) {
            String sql = "INSERT INTO mensagens (Code, Mensagem) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (String msg : mensagens) {
                String[] parts = msg.split(" ", 2);
                int mid = Integer.parseInt(parts[0]);
                String mensagem = parts[1];
                pstmt.setInt(1, mid);
                pstmt.setString(2, mensagem);
                pstmt.executeUpdate();
            }
            System.out.println("Data inserted successfully.");
        }

    }

    public static int getMidFromCode(String code) throws SQLException {
        Connection conn = getConnection();
        String query = "SELECT MID FROM mensagens WHERE code = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, code);
        ResultSet resultSet = statement.executeQuery();
        int mid = resultSet.getInt("MID");
        conn.close();

        return mid;
    }

    public static void log(String code, String field1, String field2) throws SQLException {
        Connection connection = getConnection();
        String insertSQL = "INSERT INTO registro (MID, \"campo 1\", \"campo 2\", time) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(insertSQL);

        statement.setInt(1, getMidFromCode(code));
        statement.setString(2, field1);
        statement.setString(3, field2);
        statement.setLong(4, System.currentTimeMillis());

        statement.executeUpdate();
        connection.close();
    }

    public static void log(String code, String field1) throws SQLException {
        log(code, field1, null);
    }

    public static void log(String code) throws SQLException {
        log(code, null);
    }

    public static int getUserAccessCount(String login) throws SQLException{
        Connection conn = getConnection();
        int midRelevante = getMidFromCode("5001");
        String query = "SELECT COUNT(*) AS count FROM registro WHERE MID = ? AND \"campo 1\" = ?";
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, midRelevante);
        statement.setString(2, login);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            int count = resultSet.getInt("count");
            conn.close();
            return count;
        }
        conn.close();
        return 0;
    }

    public static void main(String[] args) throws Exception{
        /* String login = "admin@inf1416.puc-rio.br";
        String key = getUserTotpKey(login);
        System.out.println(key);*/
        System.out.println(getUserAccessCount("fitos"));
    }
}
