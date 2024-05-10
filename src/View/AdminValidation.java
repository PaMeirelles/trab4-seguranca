package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class AdminValidation extends JFrame {
    public AdminValidation(Consumer<String> submitFunction) {
        JFrame frame = new JFrame("Admin Validation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Insira a frase secreta do administrador:");
        frame.add(label);

        JTextField textField = new JTextField(20); // 20 columns wide
        frame.add(textField);

        JButton button = new JButton("Submit");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputText = textField.getText();
                submitFunction.accept(inputText);
                frame.dispose();
            }
        });
        frame.add(button);

        frame.setVisible(true);
    }
}
