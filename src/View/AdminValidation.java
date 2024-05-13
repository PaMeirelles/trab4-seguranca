package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class AdminValidation extends JFrame {
    public static String secretPhraseInput() {
        JDialog dialog = new JDialog((Frame) null, "Insira a frase secreta do admnistrador", true);
        JTextField textField = new JTextField(20);
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancelar");

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("Insira a frase secreta:"));
        panel.add(textField);
        panel.add(okButton);
        panel.add(cancelButton);

        dialog.getContentPane().add(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(null);

        // Inicialmente desabilita o botão OK até que algo seja digitado.
        okButton.setEnabled(false);

        // Listener para o campo de texto para habilitar o botão OK quando algo for digitado.
        textField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                okButton.setEnabled(!textField.getText().trim().isEmpty());
            }
        });

        // Define a ação do botão OK.
        okButton.addActionListener(e -> dialog.dispose());

        // Define a ação do botão Cancelar para fechar a aplicação.
        cancelButton.addActionListener(e -> System.exit(0));

        dialog.setVisible(true);

        // Retorna o texto inserido se OK foi pressionado e o texto não está vazio.
        if (!textField.getText().trim().isEmpty()) {
            return textField.getText().trim();
        } else {
            System.exit(0);
            return null; // Esta linha não é realmente necessária, mas mantém o compilador feliz.
        }
    }
}
