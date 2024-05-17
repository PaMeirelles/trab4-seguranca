package View;

import Controller.RegistrationCallback;
import Model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        setSize(400, 300);
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
                    callback.onSubmit(certPath, keyPath, secretPhrase, group, password, confirmPassword);
                    registrationSuccessful = true; // Registration was successful
                    dispose(); // Close the dialog
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

        if(!isFirstAccess){
            add(buttonGoBack);
        }

        setVisible(true);
    }

    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }

    public boolean isGoBackPressed() {
        return goBackPressed;
    }
}
