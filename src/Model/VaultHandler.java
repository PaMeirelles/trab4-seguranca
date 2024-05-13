package Model;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.security.Key;
import java.security.PrivateKey;
import java.security.SecureRandom;

public class VaultHandler {
    public static boolean decryptIndex(String pathFolder, String secretPhrase, PrivateKey pk) throws Exception {
        Register r = new Register();

        // Validar o administrador com a frase secreta fornecida
        if (!r.validateAdmin(secretPhrase)) {
            return false;
        }

        // Caminho completo para o arquivo de índice cifrado
        String encryptedIndexPath = pathFolder + File.separator + "index.env";

        // Verificar se o arquivo de índice cifrado existe
        File encryptedIndexFile = new File(encryptedIndexPath);
        if (!encryptedIndexFile.exists()) {
            throw new Exception("O arquivo de índice cifrado não foi encontrado.");
        }

        // Decifrar o arquivo de índice
        Cipher cipher = Cipher.getInstance("RSA"); // Usar RSA para decifrar com a chave privada
        cipher.init(Cipher.DECRYPT_MODE, pk);
        FileInputStream fis = new FileInputStream(encryptedIndexFile);
        byte[] encryptedData = new byte[(int) encryptedIndexFile.length()];
        fis.read(encryptedData);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        fis.close();

        // Verificar integridade e autenticidade do arquivo de índice (pode incluir verificação de MAC ou assinatura digital)
        // Aqui, vamos supor que a verificação já foi feita com sucesso

        // Listar o conteúdo do arquivo de índice decifrado
        String decryptedIndexContent = new String(decryptedData);
        System.out.println("Conteúdo do arquivo de índice decifrado:");
        System.out.println(decryptedIndexContent);

        SecureRandom rand = SecureRandom.getInstance(Constants.SECURE_RANDOM_ALGO);
        rand.setSeed(decryptedIndexContent.getBytes());

        KeyGenerator keyGen = KeyGenerator.getInstance(Constants.KEY_GENERATOR_ALGO);
        keyGen.init(Constants.KEY_SIZE, rand);
        Key chave = keyGen.generateKey();

        cipher = Cipher.getInstance(Constants.CYPHER_TRANSFORMATION); // Usar RSA para decifrar com a chave privada
        cipher.init(Cipher.DECRYPT_MODE, chave);
        encryptedIndexPath = pathFolder + File.separator + "index.enc";
        // Verificar se o arquivo de índice cifrado existe
        encryptedIndexFile = new File(encryptedIndexPath);
        fis = new FileInputStream(encryptedIndexFile);
        encryptedData = new byte[(int) encryptedIndexFile.length()];
        fis.read(encryptedData);
        decryptedData = cipher.doFinal(encryptedData);
        fis.close();

        // Verificar integridade e autenticidade do arquivo de índice (pode incluir verificação de MAC ou assinatura digital)
        // Aqui, vamos supor que a verificação já foi feita com sucesso

        // Listar o conteúdo do arquivo de índice decifrado
        decryptedIndexContent = new String(decryptedData);
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
