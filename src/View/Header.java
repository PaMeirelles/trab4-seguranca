package View;

import Model.DatabaseManager;
import Model.Group;

import javax.swing.*;
import java.sql.*;
import java.awt.*;

public class Header extends JPanel {
    public Header(String login, Group group)throws SQLException{
        setLayout(new GridLayout(3, 1, 10, 5));
        String name;
        String groupString;
        if (group == Group.ADMIN){
                groupString = new String ("ADMIN");
            }
            else {
                groupString = new String ("USER");
            }
        name = DatabaseManager.getUserName(login);
        JLabel lb_login = new JLabel("Login: " + login);
        JLabel lb_group = new JLabel("Grupo: " + groupString);
        JLabel lb_name = new JLabel("Nome: " + name);
        
        add(lb_login);
        add(lb_group);
        add(lb_name);
    }
}