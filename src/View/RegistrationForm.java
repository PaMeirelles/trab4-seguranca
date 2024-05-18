package View;

import Controller.RegistrationCallback;
import Model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import static Model.CertificateInfo.extractCommonName;
import static Model.CertificateInfo.extractEmail;

public class RegistrationForm extends JDialog {
    private JTextField textFieldCertPath;
    private JTextField textFieldKeyPath;
    private JTextField textFieldSecretPhrase;
    private JComboBox<String> comboBoxGroup;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private boolean registrationSuccessful;
    private boolean goBackPressed;

    public RegistrationForm(Frame owner, boolean isFirstAccess, RegistrationCallback callback) {
        super(owner, "Cadastro de Usuário", true); // true for modal
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLayout(new GridLayout(8, 2, 10, 10));

        JLabel labelCertPath = new JLabel("Caminho do arquivo do certificado digital:");
        textFieldCertPath = new JTextField();
        add(labelCertPath);
        add(textFieldCertPath);

        JLabel labelKeyPath = new JLabel("Caminho do arquivo da chave privada:");
        textFieldKeyPath = new JTextField();
        add(labelKeyPath);
        add(textFieldKeyPath);

        JLabel labelSecretPhrase = new JLabel("Frase secreta:");
        textFieldSecretPhrase = new JTextField();
        add(labelSecretPhrase);
        add(textFieldSecretPhrase);

        JLabel labelGroup = new JLabel("Grupo:");
        if (isFirstAccess) {
            comboBoxGroup = new JComboBox<>(new String[]{"ADMIN"});
        } else {
            comboBoxGroup = new JComboBox<>(new String[]{"ADMIN", "USER"});
        }
        add(labelGroup);
        add(comboBoxGroup);

        JLabel labelPassword = new JLabel("Senha pessoal:");
        passwordField = new JPasswordField();
        add(labelPassword);
        add(passwordField);

        JLabel labelConfirmPassword = new JLabel("Confirmação senha pessoal:");
        confirmPasswordField = new JPasswordField();
        add(labelConfirmPassword);
        add(confirmPasswordField);

        JButton buttonRegister = new JButton("Submit");
        buttonRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String certPath = textFieldCertPath.getText();
                    String keyPath = textFieldKeyPath.getText();
                    String secretPhrase = textFieldSecretPhrase.getText();
                    String group = (String) comboBoxGroup.getSelectedItem();
                    String password = new String(passwordField.getPassword());
                    String confirmPassword = new String(confirmPasswordField.getPassword());

                    // Check if passwords match
                    if (!password.equals(confirmPassword)) {
                        throw new PasswordMismatchException();
                    }

                    // Extract certificate details
                    X509Certificate certificate = loadCertificate(certPath);
                    if (certificate == null) {
                        throw new InvalidPrivateKeyException();
                    }

                    if (showConfirmationDialog(certificate)) {
                        String totpKey = callback.onSubmit(certPath, keyPath, secretPhrase, group, password, confirmPassword);
                        registrationSuccessful = true;
                        dispose();
                        showTotpKeyDialog(totpKey);
                    }

                } catch (PasswordMismatchException ex) {
                    JOptionPane.showMessageDialog(null, "Senhas não são iguais");
                } catch (InvalidPasswordFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Formato inválido. Senhas devem ser númericas e possuir entre 8 e 10 dígitos");
                } catch (InvalidPrivateKeyException ex) {
                    JOptionPane.showMessageDialog(null, "Chave privada inválida");
                } catch (RepeatingCharactersException ex) {
                    JOptionPane.showMessageDialog(null, "Senhas não podem possuir dígitos repetidos");
                } catch (LoginNotUniqueException ex) {
                    JOptionPane.showMessageDialog(null, "Login já cadastrado");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        add(buttonRegister);

        JButton buttonGoBack = new JButton("Go Back");
        buttonGoBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackPressed = true;
                dispose(); // Close the dialog
            }
        });

        if (!isFirstAccess) {
            add(buttonGoBack);
        }

        setVisible(true);
    }
    private void showTotpKeyDialog(String totpKey) {
        JOptionPane.showMessageDialog(this, "Your TOTP key is: " + totpKey, "TOTP Key",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private X509Certificate loadCertificate(String certPath) {
        try (InputStream inStream = new FileInputStream(certPath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(inStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean showConfirmationDialog(X509Certificate certificate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder certDetails = new StringBuilder();
        certDetails.append("Versão: ").append(certificate.getVersion()).append("\n");
        certDetails.append("Série: ").append(certificate.getSerialNumber().toString(16)).append("\n");
        certDetails.append("Validade: ").append(dateFormat.format(certificate.getNotBefore())).append(" - ").append(dateFormat.format(certificate.getNotAfter())).append("\n");
        certDetails.append("Tipo de Assinatura: ").append(certificate.getSigAlgName()).append("\n");
        certDetails.append("Emissor: ").append(extractCommonName(certificate.getIssuerX500Principal().getName())).append("\n");
        certDetails.append("Sujeito: ").append(extractCommonName(certificate.getSubjectX500Principal().getName())).append("\n");
        certDetails.append("E-mail: ").append(extractEmail(certificate.getSubjectX500Principal().getName())).append("\n");

        int result = JOptionPane.showConfirmDialog(this, certDetails.toString(), "Confirmação do Certificado Digital", JOptionPane.OK_CANCEL_OPTION);
        return result == JOptionPane.OK_OPTION;
    }

    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }

    public boolean isGoBackPressed() {
        return goBackPressed;
    }
}