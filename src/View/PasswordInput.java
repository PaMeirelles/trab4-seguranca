package View;


import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.*;


public class PasswordInput{
    
    

    public static List<String> createAndShowGUI() {
        JDialog frame = new JDialog((Frame) null, true);
        frame.setTitle("Teclado Virtual");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.setLocationRelativeTo(null);
        List<String>[] buttonLabels = new List[]{generateLabels()};
        List<String>[] possiblePasswords = new List[]{new ArrayList<>()}; // Initialize an empty ArrayList
        List<String> buffer = new ArrayList<>(); // Initialize an empty ArrayList
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 15, 5);
        
        possiblePasswords[0].add("");
    
        JTextField passwordField = new JTextField(20);
        passwordField.setEditable(false);

        JPanel keyboardPanel = new JPanel(new GridLayout(2, 5, 5, 5));

        JButton numPad_1 = new JButton();
        JButton numPad_2 = new JButton();
        JButton numPad_3 = new JButton();
        JButton numPad_4 = new JButton();
        JButton numPad_5 = new JButton();

        numPad_1.addActionListener(e -> {
            passwordField.setText(passwordField.getText() + '*');
            buffer.clear();
            for(String password : possiblePasswords[0]){
                buffer.add(password + numPad_1.getText().charAt(0));
                buffer.add(password + numPad_1.getText().charAt(2));
            }
            possiblePasswords[0].clear();
            possiblePasswords[0].addAll(buffer);

            buttonLabels[0] = generateLabels();
            numPad_1.setText(buttonLabels[0].get(0));
            numPad_2.setText(buttonLabels[0].get(1));
            numPad_3.setText(buttonLabels[0].get(2));
            numPad_4.setText(buttonLabels[0].get(3));
            numPad_5.setText(buttonLabels[0].get(4));
        });
        numPad_2.addActionListener(e -> {
            passwordField.setText(passwordField.getText() + '*');
            buffer.clear();
            for(String password : possiblePasswords[0]){
                buffer.add(password + numPad_2.getText().charAt(0));
                buffer.add(password + numPad_2.getText().charAt(2));
            }
            possiblePasswords[0].clear();
            possiblePasswords[0].addAll(buffer);

            buttonLabels[0] = generateLabels();
            numPad_1.setText(buttonLabels[0].get(0));
            numPad_2.setText(buttonLabels[0].get(1));
            numPad_3.setText(buttonLabels[0].get(2));
            numPad_4.setText(buttonLabels[0].get(3));
            numPad_5.setText(buttonLabels[0].get(4));
        });
        numPad_3.addActionListener(e -> {
            passwordField.setText(passwordField.getText() + '*');
            buffer.clear();
            for(String password : possiblePasswords[0]){
                buffer.add(password + numPad_3.getText().charAt(0));
                buffer.add(password + numPad_3.getText().charAt(2));
            }
            possiblePasswords[0].clear();
            possiblePasswords[0].addAll(buffer);

            buttonLabels[0] = generateLabels();
            numPad_1.setText(buttonLabels[0].get(0));
            numPad_2.setText(buttonLabels[0].get(1));
            numPad_3.setText(buttonLabels[0].get(2));
            numPad_4.setText(buttonLabels[0].get(3));
            numPad_5.setText(buttonLabels[0].get(4));
        });
        numPad_4.addActionListener(e -> {
            passwordField.setText(passwordField.getText() + '*');
            buffer.clear();
            for(String password : possiblePasswords[0]){
                buffer.add(password + numPad_4.getText().charAt(0));
                buffer.add(password + numPad_4.getText().charAt(2));
            }
            possiblePasswords[0].clear();
            possiblePasswords[0].addAll(buffer);

            buttonLabels[0] = generateLabels();
            numPad_1.setText(buttonLabels[0].get(0));
            numPad_2.setText(buttonLabels[0].get(1));
            numPad_3.setText(buttonLabels[0].get(2));
            numPad_4.setText(buttonLabels[0].get(3));
            numPad_5.setText(buttonLabels[0].get(4));
        });
        numPad_5.addActionListener(e -> {
            passwordField.setText(passwordField.getText() + '*');
            buffer.clear();
            for(String password : possiblePasswords[0]){
                buffer.add(password + numPad_5.getText().charAt(0));
                buffer.add(password + numPad_5.getText().charAt(2));
            }
            possiblePasswords[0].clear();
            possiblePasswords[0].addAll(buffer);

            buttonLabels[0] = generateLabels();
            numPad_1.setText(buttonLabels[0].get(0));
            numPad_2.setText(buttonLabels[0].get(1));
            numPad_3.setText(buttonLabels[0].get(2));
            numPad_4.setText(buttonLabels[0].get(3));
            numPad_5.setText(buttonLabels[0].get(4));
        });

        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            possiblePasswords[0].clear();
            passwordField.setText("");
        });

        JButton enter = new JButton("Enter");
        enter.addActionListener(e -> frame.dispose());

        numPad_1.setText(buttonLabels[0].get(0));
        numPad_2.setText(buttonLabels[0].get(1));
        numPad_3.setText(buttonLabels[0].get(2));
        numPad_4.setText(buttonLabels[0].get(3));
        numPad_5.setText(buttonLabels[0].get(4));

        keyboardPanel.add(numPad_1);
        keyboardPanel.add(numPad_2);
        keyboardPanel.add(numPad_3);
        keyboardPanel.add(numPad_4);
        keyboardPanel.add(numPad_5);
        keyboardPanel.add(new JLabel(""));
        keyboardPanel.add(clear);
        keyboardPanel.add(enter);
        
        gbc.gridy = 0;
        frame.add(passwordField, gbc);
        
        gbc.gridy = 1;
        frame.add(keyboardPanel, gbc);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);


        return possiblePasswords[0];
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
        return labels;
    }
    public static void main(String[] args) throws SQLException {
        createAndShowGUI();
    }
}
