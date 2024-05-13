package Model;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.SecureRandom;

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
        return new String(decryptedData);
    }

    public static boolean decryptIndex(String pathFolder, String secretPhrase, PrivateKey pk) throws Exception {
        Register r = new Register();

        if (!r.validateAdmin(secretPhrase)) {
            return false;
        }
        Cipher cipher;

        cipher = Cipher.getInstance(Constants.KEY_ALGO); // Usar RSA para decifrar com a chave privada

        String decryptedEnv = decodeAndRead(cipher, pk, pathFolder, "index.env");
        System.out.println("Conteúdo do envelope do índice decifrado:");
        System.out.println(decryptedEnv);

        SecureRandom rand = SecureRandom.getInstance(Constants.SECURE_RANDOM_ALGO);
        rand.setSeed(decryptedEnv.getBytes());

        KeyGenerator keyGen = KeyGenerator.getInstance(Constants.KEY_GENERATOR_ALGO);
        keyGen.init(Constants.KEY_SIZE, rand);
        Key chave = keyGen.generateKey();

        cipher = Cipher.getInstance(Constants.CYPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, chave);
        String decryptedIndexContent = decodeAndRead(cipher, chave, pathFolder, "index.enc");
        System.out.println("Conteúdo do arquivo de índice decifrado:");
        System.out.println(decryptedIndexContent);
        return true;
    }
    public static void main(String[] args) throws Exception {
        Register r = new Register();
        r.validateAdmin("admin");
        decryptIndex("D:\\Segurança\\trab4-seguranca\\Pacote-T4\\Files", "admin", r.privateKey);
    }
}
