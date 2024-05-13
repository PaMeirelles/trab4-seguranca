package Model;

import javax.crypto.*;
import java.io.*;
import java.security.*;
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

    private static byte[] decryptFile(String pathFolder, PrivateKey pk, String fileName) throws Exception {
        Cipher cipher;

        cipher = Cipher.getInstance(Constants.KEY_ALGO);

        String decryptedEnv = new String(decodeAndRead(cipher, pk, pathFolder, fileName + ".env"));

        SecureRandom rand = SecureRandom.getInstance(Constants.SECURE_RANDOM_ALGO);
        rand.setSeed(decryptedEnv.getBytes());

        KeyGenerator keyGen = KeyGenerator.getInstance(Constants.KEY_GENERATOR_ALGO);
        keyGen.init(Constants.KEY_SIZE, rand);
        Key chave = keyGen.generateKey();

        cipher = Cipher.getInstance(Constants.CYPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, chave);
        return decodeAndRead(cipher, chave, pathFolder, fileName + ".enc");

    }

    public static void writeToFile(byte[] content, String pathFolder, String fileName) throws IOException {
        File outputFile = new File(pathFolder, fileName);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(content);
        }
    }

    private static boolean checkIntegrity(String folder, String name, Key key){
        // TODO
        return true;
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
            if (Objects.equals(parts[3], "administrator")){
                g = Group.ADMIN;
            }
            else {
                // TODO: Tratar o caso de nenhum dos dois
                g = Group.USER;
            }
            secretFile.group = g; // Assuming Group constructor takes the group name
            secretFiles.add(secretFile);
        }

        return secretFiles;
    }

    public static List<SecretFile> decodeIndex(String secretPhrase, String folderPath) throws Exception {
        // TODO: Tratar adequadamente os casos de erro
        Register r = new Register();
        boolean phraseValid = r.validateAdmin(secretPhrase);
        boolean integrity = checkIntegrity(folderPath, "index.asd", r.privateKey);
        if (!phraseValid || !integrity){
            throw new Exception();
        }
        byte[] indexInfo = decryptFile(folderPath, r.privateKey, "index");
        return parseSecretFiles(new String(indexInfo));
    }

    public static void decodeFile(String pathFolder, String loggedUser, SecretFile sf, PrivateKey key) throws Exception {
        boolean integrity = checkIntegrity(pathFolder, sf.fakeName + ".asd", key);
        // TODO
        if(!Objects.equals(loggedUser, sf.owner) || !integrity){
            throw new Exception();
        }
        byte[] content = decryptFile(pathFolder, key, sf.fakeName);
        writeToFile(content, pathFolder, sf.trueName);
    }

    public static void main(String[] args) throws Exception {
        Register r = new Register();
        r.validateAdmin("admin");
        List<SecretFile> files = decodeIndex("admin", "D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Files");
        decodeFile("D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Files", "admin@inf1416.puc-rio.br", files.get(0), r.privateKey);
    }
}
