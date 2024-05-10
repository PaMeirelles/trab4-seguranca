package View;

import javax.swing.*;
import java.awt.*;

public class Registration extends JFrame{
    public Registration() {
        JFrame frame = new JFrame("Input Box Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 100);
        frame.setLayout(new FlowLayout());

        // Create the input box (JTextField)
        JTextField textField = new JTextField(20); // 20 columns wide
        frame.add(textField);

        // Create a button (optional)
        JButton button = new JButton("Submit");
        frame.add(button);

        // Set JFrame visibility
        frame.setVisible(true);
    }
}
