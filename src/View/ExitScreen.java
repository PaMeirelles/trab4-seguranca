package View;

import Model.*;
import Controller.*;
import org.bouncycastle.util.test.FixedSecureRandom;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ExitScreen {
    public static void createAndShowGUI(String login, Group group) throws SQLException {
        JFrame frame = new JFrame("Tela de Saida");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 15, 5);

        Header head = new Header(login, group);
        gbc.gridy = 0;
        frame.add(head, gbc);

        gbc.gridy = 1;
        frame.add(new JSeparator(), gbc);

        int aCount = DatabaseManager.getUserAccessCount(login);
        JPanel access_count = new JPanel();
        access_count.setLayout(new GridLayout(1, 1, 10, 10));
        access_count.add(new JLabel("Total de acessos do usuario: " + aCount));

        gbc.gridy = 2;
        frame.add(access_count, gbc);

        gbc.gridy = 3;
        frame.add(new JSeparator(), gbc);
        
        gbc.gridy = 4;
        frame.add(new JLabel("Saida do Sistema: "), gbc);
        
        gbc.gridy = 5;
        frame.add(new JLabel("Pressione o botao Encerrar Sessao ou o botao Encerrar Sistema para confirmar"), gbc);
        
        JPanel exit_pan = new JPanel();
        exit_pan.setLayout(new GridLayout(2, 2, 10, 10));

        JButton endSession = new JButton("Encerrar Sessao");
        JButton endSys = new JButton("Encerrar Sistema");
        JButton backToMenu = new JButton("Voltar ao Menu Principal");
        
        endSession.addActionListener(e -> {
            close_session();
        });

        endSys.addActionListener(e -> {
            close_sys();
        });

        backToMenu.addActionListener(e -> {
            try {
                frame.dispose();
                MainMenu.createAndShowGUI(login, Main.frase_secreta);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        exit_pan.add(endSession);
        exit_pan.add(endSys);
        exit_pan.add(backToMenu);
        
        gbc.gridy = 6;
        frame.add(exit_pan, gbc);
        frame.setVisible(true);
    }
    private static void close_session(){
        // TODO
    }
    private static void close_sys(){
        // TODO
    }
}