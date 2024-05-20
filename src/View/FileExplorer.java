package View;

import Model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.sql.SQLException;


import static Model.VaultHandler.decodeFile;
import static Model.VaultHandler.decodeIndex;

public class FileExplorer {
    public static void createAndShowGUI(String login, String secretPhrase) throws SQLException {
        DatabaseManager.log("7001", login);
        JFrame frame = new JFrame("Tela de Consultar Pasta de Arquivos");
        frame.setSize(900, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 75, 20, 75);

        Header head = new Header(login, DatabaseManager.getUserGroup(login));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        frame.add(head, gbc);

        gbc.gridy = 1;
        frame.add(new JSeparator(), gbc);

        int aCount = DatabaseManager.getUserQueryCount(login);
        JPanel access_count = new JPanel();
        access_count.setLayout(new GridLayout(1, 1, 10, 10));
        access_count.add(new JLabel("Total de acessos do usuario: " + aCount));

        gbc.gridy = 2;
        gbc.gridwidth = 3;
        frame.add(access_count, gbc);

        JPanel inter = new JPanel();
        inter.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel folderPathLabel = new JLabel("Caminho da Pasta:");
        JTextField folderPathField = new JTextField();

        JLabel secretPhraseLabel = new JLabel("Frase Secreta:");
        JTextField secretPhraseField = new JTextField();

        JButton showList = new JButton("Listar");

        inter.add(folderPathLabel);
        inter.add(folderPathField);
        inter.add(secretPhraseLabel);
        inter.add(secretPhraseField);
        inter.add(new JLabel()); // Empty label to align the button
        inter.add(showList);

        gbc.gridy = 3;
        gbc.gridwidth = 3;
        frame.add(inter, gbc);

        // Table panel
        JTable table = new JTable(); // Replace with actual table model
        JScrollPane tableScrollPane = new JScrollPane(table);

        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        frame.add(tableScrollPane, gbc);

        JButton backToMenu = new JButton("Voltar ao Menu Principal");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JScrollPane buttonScrollPane = new JScrollPane(buttonPanel);
        frame.add(buttonScrollPane);
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        frame.add(backToMenu, gbc);

        showList.addActionListener(e -> {
            try {
                DatabaseManager.log("7003", login);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            String folderPath = folderPathField.getText();
            String secretPhrase1 = secretPhraseField.getText();
            try {
                List<SecretFile> secretFiles = decodeIndex(secretPhrase1, folderPath);
                PublicKey publicKey = DatabaseManager.retrievePublicKey(login);
                PrivateKey privateKey = Register.genPrivateKey(DatabaseManager.retrieveprivateKeyBytes(login), false, secretPhrase1);
                getTable(publicKey, privateKey, login, folderPath, table, secretFiles, buttonPanel);

            }
            catch (InvalidPhraseException ex){
                JOptionPane.showMessageDialog(frame, "Frase secreta incorreta");
            }
            catch(FilePathNotFoundException ex){
                try {
                    DatabaseManager.log("7004", login);
                } catch (SQLException exc) {
                    throw new RuntimeException(exc);
                }
                JOptionPane.showMessageDialog(frame, "Caminho da pasta incorreto");
            }
            catch(IntegrityCheckFailedException ex){
                JOptionPane.showMessageDialog(frame, "Falha no teste de integridade");
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });

        backToMenu.addActionListener(e -> {
            try {
                DatabaseManager.log("7002", login);
                frame.dispose();
                MainMenu.createAndShowGUI(login, secretPhrase);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
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

            JButton button = new JButton("Decrypt " + file.fakeName);
            buttonPanel.add(button);

            button.addActionListener(e -> {
                try {
                    decodeFile(pathFolder, loggedUser, file, privateKey, publicKey);
                }
                catch (InvalidPhraseException ex){
                    JOptionPane.showMessageDialog(null, "Frase secreta incorreta");
                }
                catch(IntegrityCheckFailedException ex){
                    JOptionPane.showMessageDialog(null, "Falha no teste de integridade");
                }
                catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
        table.setModel(model);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }
}