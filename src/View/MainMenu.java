package View;

import Model.Register;

import javax.swing.*;
import java.awt.*;

public class MainMenu {
    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Menu Principal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 1));

        JButton cadastrarUsuarioButton = new JButton("1 - Cadastrar um novo usuário");
        JButton consultarPastaButton = new JButton("2 - Consultar pasta de arquivos secretos do usuário");
        JButton sairButton = new JButton("3 - Sair do Sistema");

        cadastrarUsuarioButton.addActionListener(e -> {
            Register r = new Register();
            new RegistrationForm((certPath, keyPath, secretPhrase, group, password, confirmPassword) -> {
                r.fillInfo(certPath, keyPath, secretPhrase, group, password, confirmPassword);
                r.registerAdmin();
            });
        });

        consultarPastaButton.addActionListener(e -> {
            displayConsultarArquivos();
        });

        sairButton.addActionListener(e -> {
            telaDeSaida();
        });

        panel.add(cadastrarUsuarioButton);
        panel.add(consultarPastaButton);
        panel.add(sairButton);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private static void displayConsultarArquivos(){
        // TODO
    }
    private static void telaDeSaida(){
        // TODO
    }
}
