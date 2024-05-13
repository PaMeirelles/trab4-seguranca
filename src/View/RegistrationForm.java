package View;

import Controller.RegistrationCallback;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrationForm extends JFrame {
    private JTextField textFieldCertPath;
    private JTextField textFieldKeyPath;
    private JTextField textFieldSecretPhrase;
    private JComboBox<String> comboBoxGroup;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegistrationForm(RegistrationCallback callback) {
        setTitle("Cadastro de Usuário");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLayout(new GridLayout(7, 2, 10, 10));

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
        comboBoxGroup = new JComboBox<>(new String[]{"ADMIN", "USER"});
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
                String certPath = textFieldCertPath.getText();
                String keyPath = textFieldKeyPath.getText();
                String secretPhrase = textFieldSecretPhrase.getText();
                String group = (String) comboBoxGroup.getSelectedItem();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                try {
                    callback.onSubmit(certPath, keyPath, secretPhrase, group, password, confirmPassword);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        add(buttonRegister);

        setVisible(true);
    }
}
