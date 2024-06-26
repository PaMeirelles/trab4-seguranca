package View;

import Controller.*;
import Model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import static Model.CertificateInfo.extractCommonName;
import static Model.CertificateInfo.extractEmail;
import static Model.DatabaseManager.getUserGroup;

public class RegistrationForm extends JDialog{
    private final JTextField textFieldCertPath;
    private final JTextField textFieldKeyPath;
    private final JTextField textFieldSecretPhrase;
    private final JComboBox<String> comboBoxGroup;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;
    private boolean registrationSuccessful;
    private boolean goBackPressed;

    public RegistrationForm(String login, Frame owner, boolean isFirstAccess, RegistrationCallback callback) throws SQLException {
        super(owner, "Cadastro de Usuario", true);
        DatabaseManager.log("6001", login);
        
        setSize(600, 600);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 15, 5);

        Header head = new Header(login, getUserGroup(login));
        gbc.gridy = 0;
        add(head, gbc);

        gbc.gridy = 1;
        add(new JSeparator(), gbc);

        int userCount = DatabaseManager.getUserAccessCount(login);
        JPanel user_count = new JPanel();
        user_count.setLayout(new GridLayout(1, 1, 10, 10));
        user_count.add(new JLabel("Total de usuarios do sistema: " + userCount));

        gbc.gridy = 2;
        add(user_count, gbc);

        gbc.gridy = 3;
        add(new JSeparator(), gbc);

        gbc.gridy = 4;
        add(new JLabel("Formulario de Cadastro: "), gbc);

        JPanel form = new JPanel();
        form.setLayout(new GridLayout(7, 1, 10, 10));
        
        JLabel labelCertPath = new JLabel("Caminho do arquivo do certificado digital:");
        textFieldCertPath = new JTextField();
        form.add(labelCertPath);
        form.add(textFieldCertPath);

        JLabel labelKeyPath = new JLabel("Caminho do arquivo da chave privada:");
        textFieldKeyPath = new JTextField();
        form.add(labelKeyPath);
        form.add(textFieldKeyPath);

        JLabel labelSecretPhrase = new JLabel("Frase secreta:");
        textFieldSecretPhrase = new JTextField();
        form.add(labelSecretPhrase);
        form.add(textFieldSecretPhrase);

        JLabel labelGroup = new JLabel("Grupo:");
        if (isFirstAccess) {
            comboBoxGroup = new JComboBox<>(new String[]{"ADMIN"});
        } else {
            comboBoxGroup = new JComboBox<>(new String[]{"ADMIN", "USER"});
        }
        form.add(labelGroup);
        form.add(comboBoxGroup);

        JLabel labelPassword = new JLabel("Senha pessoal:");
        passwordField = new JPasswordField();
        form.add(labelPassword);
        form.add(passwordField);

        JLabel labelConfirmPassword = new JLabel("Confirmacao senha pessoal:");
        confirmPasswordField = new JPasswordField();
        form.add(labelConfirmPassword);
        form.add(confirmPasswordField);

        JButton buttonRegister = getjButton(login, callback);
        form.add(buttonRegister);

        JButton buttonGoBack = getjButton(login);

        if (!isFirstAccess) {
            form.add(buttonGoBack);
        }

        gbc.gridy = 5;
        add(form, gbc);
        setVisible(true);
    }

    private JButton getjButton(String login) {
        JButton buttonGoBack = new JButton("Go Back");
        buttonGoBack.addActionListener(e -> {
            goBackPressed = true;
            try {
                DatabaseManager.log("6010", login);
                dispose();
                MainMenu.createAndShowGUI(login, Main.frase_secreta);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        return buttonGoBack;
    }

    private JButton getjButton(String login, RegistrationCallback callback) {
        JButton buttonRegister = new JButton("Submit");
        buttonRegister.addActionListener(e -> {
            try {
                DatabaseManager.log("6002", login);
                String certPath = textFieldCertPath.getText();
                String keyPath = textFieldKeyPath.getText();
                String secretPhrase = textFieldSecretPhrase.getText();
                String group = (String) comboBoxGroup.getSelectedItem();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                // Extract certificate details
                X509Certificate certificate = loadCertificate(certPath);

                assert certificate != null;
                if (showConfirmationDialog(certificate, login)) {
                    String totpKey = callback.onSubmit(certPath, keyPath, secretPhrase, group, password, confirmPassword);
                    registrationSuccessful = true;
                    dispose();
                    showTotpKeyDialog(totpKey);
                }

                dispose();
                DatabaseManager.log("6001", login);
                RegistrationManager.register(false, login);

            } catch (PasswordMismatchException ex) {
                try {
                    DatabaseManager.log("6003", login);
                } catch (SQLException exc) {
                    throw new RuntimeException(exc);
                }
                JOptionPane.showMessageDialog(null, "Senhas nao sao iguais");
            } catch (InvalidPasswordFormatException ex) {
                JOptionPane.showMessageDialog(null, "Formato invalido. Senhas devem ser namericas e possuir entre 8 e 10 digitos");
            } catch (InvalidPrivateKeyException ex) {
                try {
                    if (ex.ikt == InvalidPrivateKeyException.InvalidKeyType.INVALID_PATH) {
                        DatabaseManager.log("6005", login);
                        JOptionPane.showMessageDialog(null, "Caminho invalido para chave privada");
                    }
                    else if (ex.ikt == InvalidPrivateKeyException.InvalidKeyType.INVALID_SECRET_PHRASE) {
                        DatabaseManager.log("6006", login);
                        JOptionPane.showMessageDialog(null, "Frase secreta invalida");
                    }
                    else if (ex.ikt == InvalidPrivateKeyException.InvalidKeyType.INVALID_DIGITAL_SIGNATURE){
                        DatabaseManager.log("6007", login);
                        JOptionPane.showMessageDialog(null, "Chave privada nao corresponde a chave publica fornecida");

                    }
                }
                catch (SQLException excep){
                    throw new RuntimeException(excep);
                }
            } catch (RepeatingCharactersException ex) {
                JOptionPane.showMessageDialog(null, "Senhas nao podem possuir digitos repetidos");
            } catch (LoginNotUniqueException ex) {
                JOptionPane.showMessageDialog(null, "Login ja cadastrado");
            }
            catch (CertificatePathNotFoundException ex){
                try {
                    DatabaseManager.log("6004", login);
                    JOptionPane.showMessageDialog(null, "Caminho invalido para certificado digital");
                } catch (SQLException exc) {
                    throw new RuntimeException(exc);
                }
                JOptionPane.showMessageDialog(null, "Login ja cadastrado");
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        
        return buttonRegister;
    }

    private void showTotpKeyDialog(String totpKey) {
        JOptionPane.showMessageDialog(this, "Your TOTP key is: " + totpKey, "TOTP Key",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private X509Certificate loadCertificate(String certPath) throws CertificatePathNotFoundException, CertificateException{
        try (InputStream inStream = Files.newInputStream(Paths.get(certPath))) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(inStream);
        }
        catch (IOException ex){
            throw new CertificatePathNotFoundException();
        }

    }

    private boolean showConfirmationDialog(X509Certificate certificate, String login) throws SQLException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String certDetails = "Versao: " + certificate.getVersion() + "\n" +
                "Série: " + certificate.getSerialNumber().toString(16) + "\n" +
                "Validade: " + dateFormat.format(certificate.getNotBefore()) + " - " + dateFormat.format(certificate.getNotAfter()) + "\n" +
                "Tipo de Assinatura: " + certificate.getSigAlgName() + "\n" +
                "Emissor: " + extractCommonName(certificate.getIssuerX500Principal().getName()) + "\n" +
                "Sujeito: " + extractCommonName(certificate.getSubjectX500Principal().getName()) + "\n" +
                "E-mail: " + extractEmail(certificate.getSubjectX500Principal().getName()) + "\n";

        int result = JOptionPane.showConfirmDialog(this, certDetails, "Confirmacao do Certificado Digital", JOptionPane.OK_CANCEL_OPTION);
        if(result == JOptionPane.OK_OPTION){
            DatabaseManager.log("6008", login);
        }
        else if(result == JOptionPane.CANCEL_OPTION){
            DatabaseManager.log("6009", login);
        }
        return result == JOptionPane.OK_OPTION;
    }

    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }

    public boolean isGoBackPressed() {
        return goBackPressed;
    }
}