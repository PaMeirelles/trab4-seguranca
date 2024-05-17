package View;

import javax.swing.*;
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
    private static List<String> generateLabels(){
        String chars = "0123456789";

        List<Character> charList = new ArrayList<>();
        for (char c : chars.toCharArray()) {
            charList.add(c);
        }

        Collections.shuffle(charList);

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < charList.size(); i += 2) {
            labels.add(String.valueOf(charList.get(i)) + '-' + charList.get(i + 1));
        }
        labels.add("Clear");
        labels.add("Enter");
        return labels;
    }
    public static List<String> collectPassword() {
        final List<String>[] possiblePasswords = new List[]{new ArrayList<>()}; // Initialize an empty ArrayList
        possiblePasswords[0].add("");

        List<String> buffer = new ArrayList<>(); // Initialize an empty ArrayList

        JFrame frame = new JFrame("Virtual Keyboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField passwordField = new JTextField(20);
        passwordField.setEditable(false);

        JPanel keyboardPanel = new JPanel(new GridLayout(2, 5));
        final List<String>[] buttonLabels = new List[]{generateLabels()};

        for (String label : buttonLabels[0]) {
            JButton button = new JButton(label);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String command = e.getActionCommand();
                    if (command.equals("Clear")) {
                        possiblePasswords[0].clear();

                    } else if (command.equals("Enter")) {
                        frame.dispose(); // Close the window when Enter is pressed
                    } else {
                        passwordField.setText(passwordField.getText() + '*');
                        buffer.clear();
                        for(String password : possiblePasswords[0]){
                            buffer.add(password + command.charAt(0));
                            buffer.add(password + command.charAt(2));
                        }
                        possiblePasswords[0].clear();
                        possiblePasswords[0].addAll(buffer);
                    }
                    buttonLabels[0] = generateLabels();
                    int index = 0;
                    for (Component component : keyboardPanel.getComponents()) {
                        if (component instanceof JButton) {
                            JButton button = (JButton) component;
                            button.setText(buttonLabels[0].get(index++));
                        }
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

        return possiblePasswords[0];
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

