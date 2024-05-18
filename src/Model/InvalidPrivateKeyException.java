package Model;

public class InvalidPrivateKeyException extends Exception {
    public InvalidKeyType ikt;
    public enum InvalidKeyType{
        INVALID_PATH,
        INVALID_SECRET_PHRASE,
        INVALID_DIGITAL_SIGNATURE;
    }

    public InvalidPrivateKeyException(InvalidKeyType ikt) {
        this.ikt = ikt;
    }
}
