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
    public static String collectPassword() {
        JFrame frame = new JFrame("Virtual Keyboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField passwordField = new JTextField(20);
        passwordField.setEditable(false);

        JPanel keyboardPanel = new JPanel(new GridLayout(4, 3));
        String[] buttonLabels = {
                "1", "2", "3",
                "4", "5", "6",
                "7", "8", "9",
                "Backspace", "0", "Enter"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String command = e.getActionCommand();
                    if (command.equals("Backspace")) {
                        String text = passwordField.getText();
                        if (!text.isEmpty()) {
                            passwordField.setText(text.substring(0, text.length() - 1));
                        }
                    } else if (command.equals("Enter")) {
                        frame.dispose(); // Close the window when Enter is pressed
                    } else {
                        passwordField.setText(passwordField.getText() + command);
                    }
                }
            });
            keyboardPanel.add(button);
        }

        frame.getContentPane().add(passwordField, BorderLayout.NORTH);
        frame.getContentPane().add(keyboardPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Wait until the frame is closed
        while (frame.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        return passwordField.getText();
    }
}

