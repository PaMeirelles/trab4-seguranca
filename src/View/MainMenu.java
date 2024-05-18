package View;

import Model.*;
import org.bouncycastle.util.test.FixedSecureRandom;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class MainMenu {
    public static void createAndShowGUI(String login) throws SQLException {
        DatabaseManager.log("5001", login);
        JFrame frame = new JFrame("Menu Principal");

        JPanel panel = new JPanel(new GridLayout(3, 1));

        JButton cadastrarUsuarioButton = new JButton("1 - Cadastrar um novo usuário");
        JButton consultarPastaButton = new JButton("2 - Consultar pasta de arquivos secretos do usuário");
        JButton sairButton = new JButton("3 - Sair do Sistema");

        cadastrarUsuarioButton.addActionListener(e -> {
            try {
                DatabaseManager.log("5002", login);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            RegistrationManager.register(false);
        });

        consultarPastaButton.addActionListener(e -> {
            try {
                DatabaseManager.log("5003", login);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            displayConsultarArquivos();
        });

        sairButton.addActionListener(e -> {
            try {
                DatabaseManager.log("5004", login);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
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
    public static void main(String[] args) throws SQLException {
        MainMenu.createAndShowGUI("fitos");
    }
}
