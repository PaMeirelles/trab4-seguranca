package Model;

import javax.crypto.*;
import java.io.*;
import java.security.*;

public class VaultHandler {
    private static String decodeAndRead(Cipher cipher, Key key, String folderName, String fileName) throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        String encryptedIndexPath = folderName + File.separator + fileName;
        File encryptedIndexFile = new File(encryptedIndexPath);
        cipher.init(Cipher.DECRYPT_MODE, key);
        FileInputStream fis = new FileInputStream(encryptedIndexFile);
        byte[] encryptedData = new byte[(int) encryptedIndexFile.length()];
        fis.read(encryptedData);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        fis.close();
        FileOutputStream fos = new FileOutputStream("output.docx");
        fos.write(decryptedData);
        return new String(decryptedData);
    }

    public static boolean decryptFile(String pathFolder, String secretPhrase, PrivateKey pk, String fileName) throws Exception {
        Register r = new Register();

        if (!r.validateAdmin(secretPhrase)) {
            return false;
        }
        Cipher cipher;

        cipher = Cipher.getInstance(Constants.KEY_ALGO); // Usar RSA para decifrar com a chave privada

        String decryptedEnv = decodeAndRead(cipher, pk, pathFolder, fileName + ".env");
        System.out.println("Conteúdo do envelope do índice decifrado:");
        System.out.println(decryptedEnv);

        SecureRandom rand = SecureRandom.getInstance(Constants.SECURE_RANDOM_ALGO);
        rand.setSeed(decryptedEnv.getBytes());

        KeyGenerator keyGen = KeyGenerator.getInstance(Constants.KEY_GENERATOR_ALGO);
        keyGen.init(Constants.KEY_SIZE, rand);
        Key chave = keyGen.generateKey();

        cipher = Cipher.getInstance(Constants.CYPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, chave);
        String decryptedIndexContent = decodeAndRead(cipher, chave, pathFolder, fileName + ".enc");
        System.out.println("Conteúdo do arquivo de índice decifrado:");
        System.out.println(decryptedIndexContent);
        return true;
    }

    public static void writeToFile(String content, String pathFolder, String fileName) throws IOException {
        File outputFile = new File(pathFolder, fileName);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(content.getBytes());
        }
    }
    public static void main(String[] args) throws Exception {
        Register r = new Register();
        r.validateAdmin("admin");
        decryptFile("D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Files", "admin",  r.privateKey, "XXYYZZ00");
    }
}
