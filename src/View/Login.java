package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class Login extends JFrame {
    public static String login() {
        JDialog dialog = new JDialog((Frame) null, "Login", true);
        JTextField textField = new JTextField(20);
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancelar");

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("Digite seu login:"));
        panel.add(textField);
        panel.add(okButton);
        panel.add(cancelButton);

        dialog.getContentPane().add(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(null);

        okButton.setEnabled(false);

        textField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                okButton.setEnabled(!textField.getText().trim().isEmpty());
            }
        });

        okButton.addActionListener(e -> dialog.dispose());
        cancelButton.addActionListener(e -> System.exit(0));

        dialog.setVisible(true);

        if (!textField.getText().trim().isEmpty()) {
            return textField.getText().trim();
        } else {
            System.exit(0);
            return null;
        }
    }
}

