package View;

import Model.IntegrityCheckFailedException;
import Model.InvalidPhraseException;
import Model.SecretFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import static Model.VaultHandler.decodeFile;

public class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String label;
    private boolean isPushed;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String loggedUser;
    private String pathFolder;
    private List<SecretFile> files;
    private JTable table;
    private int row;

    public ButtonEditor(JCheckBox checkBox, PublicKey publicKey, PrivateKey privateKey, String loggedUser, String pathFolder, List<SecretFile> files) {
        super(checkBox);
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.loggedUser = loggedUser;
        this.pathFolder = pathFolder;
        this.files = files;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        this.table = table;
        this.row = row;
        label = (value == null) ? "Decrypt" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            SecretFile file = files.get(row);
            try {
                decodeFile(pathFolder, loggedUser, file, privateKey, publicKey);
            } catch (InvalidPhraseException ex) {
                JOptionPane.showMessageDialog(null, "Frase secreta incorreta");
            } catch (IntegrityCheckFailedException ex) {
                JOptionPane.showMessageDialog(null, "Falha no teste de integridade");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
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
