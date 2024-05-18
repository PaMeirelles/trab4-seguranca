package Controller;

import Model.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public interface RegistrationCallback {
    String onSubmit(String certPath, String keyPath, String secretPhrase, String group, String password, String confirmPassword) throws PasswordMismatchException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, CertificateException, RepeatingCharactersException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeyException, LoginNotUniqueException, InvalidPrivateKeyException, InvalidPasswordFormatException, InvalidKeySpecException;
}
