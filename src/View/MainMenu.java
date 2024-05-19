package View;

import Model.*;
import org.bouncycastle.util.test.FixedSecureRandom;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class MainMenu {
    public static void createAndShowGUI(String login) throws SQLException {
        JFrame frame = new JFrame("MainMenu");
        frame.setTitle("Menu Principal");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 15, 5);

        Header head = new Header(login);
        gbc.gridy = 0;
        frame.add(head, gbc);

        gbc.gridy = 1;
        frame.add(new JSeparator(), gbc);

        int aCount = DatabaseManager.getUserAccessCount(login);
        JPanel access_count = new JPanel();
        access_count.setLayout(new GridLayout(1, 1, 10, 10));
        access_count.add(new JLabel("Total de acessos do usuario: " + aCount));

        gbc.gridy = 2;
        frame.add(access_count, gbc);

        gbc.gridy = 3;
        frame.add(new JSeparator(), gbc);

        JPanel menu = new JPanel();
        menu.setLayout(new GridLayout(3, 1, 10, 10));
        JButton cadastrarUsuarioButton = new JButton("1 - Cadastrar um novo usuario");
        JButton consultarPastaButton = new JButton("2 - Consultar pasta de arquivos secretos do usuário");
        JButton sairButton = new JButton("3 - Sair do Sistema");
        
        cadastrarUsuarioButton.addActionListener(e -> {
            try {
                DatabaseManager.log("5002", login);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            try {
                RegistrationManager.register(false, login);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
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

        menu.add(cadastrarUsuarioButton);
        menu.add(consultarPastaButton);
        menu.add(sairButton);
        
        gbc.gridy = 4;
        frame.add(menu, gbc);
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
