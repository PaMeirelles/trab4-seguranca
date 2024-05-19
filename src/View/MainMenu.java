package View;

import Model.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
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

        decodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderPath = folderPathField.getText();
                String secretPhrase = secretPhraseField.getText();
                try {
                    List<SecretFile> secretFiles = decodeIndex(secretPhrase, folderPath);
                    PublicKey publicKey = DatabaseManager.retrievePublicKey(login);
                    PrivateKey privateKey = Register.genPrivateKey(DatabaseManager.retrieveprivateKeyBytes(login), false, secretPhrase);
                    SecretFileTableModel tableModel = new SecretFileTableModel(secretFiles, folderPath, publicKey, privateKey, login);
                    table.setModel(tableModel);
                    table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
                    table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }
        });

        frame.setVisible(true);
    }

    static class SecretFileTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Fake Name", "True Name", "Owner", "Group", "Actions"};
        private final List<SecretFile> secretFiles;
        private final String folderPath;
        private final PublicKey publicKey;
        private final PrivateKey privateKey;
        private final String loggedUser;

        SecretFileTableModel(List<SecretFile> secretFiles, String folderPath, PublicKey publicKey, PrivateKey privateKey, String loggedUser) {
            this.secretFiles = secretFiles;
            this.folderPath = folderPath;
            this.publicKey = publicKey;
            this.privateKey = privateKey;
            this.loggedUser = loggedUser;
        }

        @Override
        public int getRowCount() {
            return secretFiles.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SecretFile sf = secretFiles.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return sf.fakeName;
                case 1:
                    return sf.trueName;
                case 2:
                    return sf.owner;
                case 3:
                    return sf.group;
                case 4:
                    JButton decryptButton = new JButton("Decrypt");
                    decryptButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                decodeFile(folderPath, loggedUser, sf, privateKey, publicKey);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                            }
                        }
                    });
                    return decryptButton;
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 4;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 4 ? JButton.class : String.class;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }
    }

    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private String label;
        private JButton button;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // the action is handled in the model listener
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    private static void telaDeSaida() {
        // TODO: implement exit logic
    }

    public static void main(String[] args) throws SQLException {
        MainMenu.createAndShowGUI("admin@inf1416.puc-rio.br");
    }
}
