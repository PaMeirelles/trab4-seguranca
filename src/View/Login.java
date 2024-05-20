package View;

import Controller.Main;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class Login extends JFrame {
    public static String login() throws SQLException {
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
        cancelButton.addActionListener(e -> {
            try {
                Main.endSystem();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        dialog.setVisible(true);

        if (!textField.getText().trim().isEmpty()) {
            return textField.getText().trim();
        } else {
            Main.endSystem();
            return null;
        }

    }

    public static String collectTOTPCode() {
        JDialog dialog = new JDialog((Frame) null, "TOTP Code", true);
        JTextField textField = new JTextField(6);
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancelar");

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("Digite o código TOTP (6 dígitos):"));
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
                String input = textField.getText().trim();
                okButton.setEnabled(input.length() == 6 && input.matches("\\d+"));
            }
        });

        okButton.addActionListener(e -> dialog.dispose());
        cancelButton.addActionListener(e -> {
            try {
                Main.endSystem();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        dialog.setVisible(true);

        if (!textField.getText().trim().isEmpty()) {
            return textField.getText().trim();
        } else {
            return null;
        }
    }

}

