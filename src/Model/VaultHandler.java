package Model;

import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VaultHandler {
    private static byte[] decodeAndRead(Cipher cipher, Key key, String folderName, String fileName) throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        String encryptedIndexPath = folderName + File.separator + fileName;
        File encryptedIndexFile = new File(encryptedIndexPath);
        cipher.init(Cipher.DECRYPT_MODE, key);
        FileInputStream fis = new FileInputStream(encryptedIndexFile);
        byte[] encryptedData = new byte[(int) encryptedIndexFile.length()];
        fis.read(encryptedData);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        fis.close();
        return decryptedData;
    }

    private static byte[] decryptFile(String pathFolder, PrivateKey pk, String fileName) throws DecryptionErrorException {
        try {
            Cipher cipher;

            cipher = Cipher.getInstance(Constants.KEY_ALGO);

            String decryptedEnv = new String(decodeAndRead(cipher, pk, pathFolder, fileName + ".env"));

            SecureRandom rand = SecureRandom.getInstance(Constants.SECURE_RANDOM_ALGO);
            rand.setSeed(decryptedEnv.getBytes());

            KeyGenerator keyGen = KeyGenerator.getInstance(Constants.KEY_GENERATOR_ALGO);
            keyGen.init(Constants.KEY_SIZE, rand);
            Key chave = keyGen.generateKey();

            cipher = Cipher.getInstance(Constants.AES_CYPHER);
            cipher.init(Cipher.DECRYPT_MODE, chave);
            return decodeAndRead(cipher, chave, pathFolder, fileName + ".enc");
        }
        catch (Exception ex){
            throw new DecryptionErrorException();
        }
    }

    public static void writeToFile(byte[] content, String pathFolder, String fileName) throws IOException {
        File outputFile = new File(pathFolder, fileName);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(content);
        }
    }

    private static byte[] fileToByte(String folder, String name) throws IOException {
        String path = folder + File.separator + name;
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fis.read(bytes);
        return bytes;
    }

    private static boolean checkIntegrity(String folder, String name, PublicKey key, byte[] content) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        Signature signature = Signature.getInstance(Constants.DIGITAL_SIGNATURE_ALGO);
        signature.initVerify(key);
        signature.update(content);
        byte[] hash = fileToByte(folder, name);
        return signature.verify(hash);
    }

    private static List<SecretFile> parseSecretFiles(String input) {
        List<SecretFile> secretFiles = new ArrayList<>();
        String[] lines = input.split("\n");

        for (String line : lines) {
            String[] parts = line.split(" ");

            // Ensure the line has the correct number of parts
            if (parts.length != 4) {
                // Handle invalid line format, such as logging an error or throwing an exception
                continue; // Skip to the next line
            }

            // Create a new SecretFile object and add it to the list
            SecretFile secretFile = new SecretFile();
            secretFile.fakeName = parts[0];
            secretFile.trueName = parts[1];
            secretFile.owner = parts[2];
            Group g;
            if (Objects.equals(parts[3], "administrador")){
                g = Group.ADMIN;
            }
            else {
                g = Group.USER;
            }
            secretFile.group = g; // Assuming Group constructor takes the group name
            secretFiles.add(secretFile);
        }

        return secretFiles;
    }

    public static List<SecretFile> decodeIndex(String secretPhrase, String folderPath, String login) throws Exception {
        Register r = new Register();
        boolean phraseValid = r.validateAdmin(secretPhrase);
        if (!phraseValid) {
            throw new InvalidPhraseException();
        }
        byte[] indexInfo = decryptFile(folderPath, r.privateKey, "index");
        DatabaseManager.log("7005", login);
        boolean integrity = checkIntegrity(folderPath, "index.asd", r.certificateInfo.publicKey, indexInfo);
        if (!integrity) {
            throw new IntegrityCheckFailedException();
        }
        return parseSecretFiles(new String(indexInfo));
    }


    public static void decodeFile(String pathFolder, String loggedUser, SecretFile sf, PrivateKey privateKey, PublicKey publicKey) throws PermissionDeniedException, DecryptionErrorException, NoSuchAlgorithmException, SignatureException, IOException, InvalidKeyException, IntegrityCheckFailedException, SQLException {
        if(!Objects.equals(loggedUser, sf.owner)){
            throw new PermissionDeniedException();
        }
        DatabaseManager.log("7011", loggedUser);
        byte[] content = decryptFile(pathFolder, privateKey, sf.fakeName);
        DatabaseManager.log("7013", loggedUser);
        boolean integrity = checkIntegrity(pathFolder, sf.fakeName + ".asd", publicKey, content);
        if(!integrity){
            throw new IntegrityCheckFailedException();
        }
        DatabaseManager.log("7014", loggedUser);
        writeToFile(content, pathFolder, sf.trueName);
    }

    public static void main(String[] args) throws Exception {
        Register r = new Register();
        //r.fillInfo("D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Keys\\user01-x509.crt", "D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Keys\\user01-pkcs8-aes.pem", "user01", "USER", "13052024", "13052024");
        r.validateSecretPhrase("admin@inf1416.puc-rio.br", "admin");
        List<SecretFile> files = decodeIndex( "admin", "D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Files", "admin@inf1416.puc-rio.br");
        decodeFile("D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Files", "admin@inf1416.puc-rio.br", files.get(0), r.privateKey, r.certificateInfo.publicKey);
    }
}
