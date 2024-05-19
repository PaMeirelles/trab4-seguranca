package View;

import Model.DatabaseManager;
import javax.swing.*;
import java.sql.*;
import java.awt.*;

public class Header extends JPanel {
    

    public Header(String login)throws SQLException{
        setLayout(new GridLayout(3, 1, 10, 5));
        String group;
        String name;
        if (DatabaseManager.getUserGroup(login) == 0){
            group = new String ("ADMIN");
            }
            else {
                group = new String ("USER");
            }
            name = DatabaseManager.getUserName(login);
        JLabel lb_login = new JLabel("Login: " + login);
        JLabel lb_group = new JLabel("Grupo: " + group);
        JLabel lb_name = new JLabel("Nome: " + name);
        
        add(lb_login);
        add(lb_group);
        add(lb_name);
    }
}