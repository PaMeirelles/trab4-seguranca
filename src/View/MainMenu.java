package View;

import Model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.sql.SQLException;

import static Model.VaultHandler.decodeFile;
import static Model.VaultHandler.decodeIndex;

public class MainMenu {

    public static void createAndShowGUI(String login) throws SQLException {
        DatabaseManager.log("5001", login);
        JFrame frame = new JFrame("Menu Principal");

        int accessCount = DatabaseManager.getUserAccessCount(login);

        JPanel panel = new JPanel(new GridLayout(4, 1));

        JLabel accessCountLabel = new JLabel("Total de acessos do usuário: " + accessCount);

        JButton cadastrarUsuarioButton = new JButton("1 - Cadastrar um novo usuário");
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
            displayConsultarArquivos(login);
        });

        sairButton.addActionListener(e -> {
            try {
                DatabaseManager.log("5004", login);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            telaDeSaida();
        });

        panel.add(accessCountLabel);
        panel.add(cadastrarUsuarioButton);
        panel.add(consultarPastaButton);
        panel.add(sairButton);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void displayConsultarArquivos(String login) {
        JFrame frame = new JFrame("File Decoder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel folderPathLabel = new JLabel("Folder Path:");
        JTextField folderPathField = new JTextField();

        JLabel secretPhraseLabel = new JLabel("Secret Phrase:");
        JTextField secretPhraseField = new JTextField();

        JButton decodeButton = new JButton("Decode");

        panel.add(folderPathLabel);
        panel.add(folderPathField);
        panel.add(secretPhraseLabel);
        panel.add(secretPhraseField);
        panel.add(decodeButton);

        frame.getContentPane().add(BorderLayout.NORTH, panel);

        JTable table = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(table);
        frame.getContentPane().add(BorderLayout.CENTER, tableScrollPane);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JScrollPane buttonScrollPane = new JScrollPane(buttonPanel);
        frame.getContentPane().add(BorderLayout.EAST, buttonScrollPane);

        decodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderPath = folderPathField.getText();
                String secretPhrase = secretPhraseField.getText();
                try {
                    List<SecretFile> secretFiles = decodeIndex(secretPhrase, folderPath);
                    PublicKey publicKey = DatabaseManager.retrievePublicKey(login);
                    PrivateKey privateKey = Register.genPrivateKey(DatabaseManager.retrieveprivateKeyBytes(login), false, secretPhrase);
                    getTable(publicKey, privateKey, login, folderPath, table, secretFiles, buttonPanel);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }
        });

        frame.setVisible(true);
    }

    private static void getTable(PublicKey publicKey, PrivateKey privateKey, String loggedUser, String pathFolder, JTable table, List<SecretFile> files, JPanel buttonPanel) {
        String[] columnNames = {"Nome código", "Nome real", "Dono", "Grupo"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        buttonPanel.removeAll();  // Clear any existing buttons

        for (SecretFile file : files) {
            Object[] rowData = {file.fakeName, file.trueName, file.owner, file.group.toString()};
            model.addRow(rowData);

            JButton button = new JButton("Decrypt" + file.fakeName);
            buttonPanel.add(button);

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        decodeFile(pathFolder, loggedUser, file, privateKey, publicKey);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
        table.setModel(model);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    private static void telaDeSaida() {
        // TODO: implement exit logic
    }

    public static void main(String[] args) throws SQLException {
        MainMenu.createAndShowGUI("admin@inf1416.puc-rio.br");
    }
}
