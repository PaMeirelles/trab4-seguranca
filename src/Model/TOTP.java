package Model;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class TOTP {
    private byte [] key = null;
    private long timeStepInSeconds = 30;

    // Construtor da classe. Recebe a chave secreta em BASE32 e o intervalo
    // de tempo a ser adotado (default = 30 segundos). Deve decodificar a
    // chave secreta e armazenar em key. Em caso de erro, gera Exception.
    public TOTP(String base32EncodedSecret, long timeStepInSeconds){
        Base32 base32 = new Base32(Base32.Alphabet.BASE32, true, false);
        this.key = base32.fromString(base32EncodedSecret);
        this.timeStepInSeconds = timeStepInSeconds;
    }

    // Recebe o HASH HMAC-SHA1 e determina o código TOTP de 6 algarismos
    // decimais, prefixado com zeros quando necessário.
    private String getTOTPCodeFromHash(byte[] hash) {
        int offset = hash[hash.length - 1] & 0xf;
        long truncatedHash = 0;

        for (int i = offset; i < offset + 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[i] & 0xff);
        }

        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;

        return String.format("%06d", truncatedHash);
    }

    // Recebe o contador e a chave secreta para produzir o hash HMAC-SHA1.
    private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha1 = Mac.getInstance(Constants.HMACSHA1);
        SecretKeySpec keySpec = new SecretKeySpec(keyByteArray, Constants.HMACSHA1);
        hmacSha1.init(keySpec);
        return hmacSha1.doFinal(counter);
    }

    private static byte[] longToBytes(long number) {
        byte[] counterBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            counterBytes[i] = (byte) (number & 0xFF);
            number >>= 8;
        }
        return counterBytes;
    }

    // Recebe o intervalo de tempo e executa o algoritmo TOTP para produzir
    // o código TOTP. Usa os métodos auxiliares getTOTPCodeFromHash e HMAC_SHA1.
    private String TOTPCode(long timeInterval) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] counter = longToBytes(timeInterval);
        byte[] hash = HMAC_SHA1(counter, key);
        return getTOTPCodeFromHash(hash);
    }

    private long getCurrentEpochInterval(){
        return System.currentTimeMillis() / timeStepInSeconds;
    }

    // Método que é utilizado para validar um código TOTP (inputTOTP).
    // Deve considerar um atraso ou adiantamento de 30 segundos no
    // relógio da máquina que gerou o código TOTP.
    public boolean validateCode(String inputTOTP) throws NoSuchAlgorithmException, InvalidKeyException {
        long currentInterval = getCurrentEpochInterval();
        long pastInterval = getCurrentEpochInterval() - 1;
        long nextInterval = getCurrentEpochInterval() + 1;

        String code1 = TOTPCode(currentInterval);
        String code2 = TOTPCode(pastInterval);
        String code3 = TOTPCode(nextInterval);

        return Objects.equals(code1, inputTOTP) ||
                Objects.equals(code2, inputTOTP) ||
                Objects.equals(code3, inputTOTP);

    }
}