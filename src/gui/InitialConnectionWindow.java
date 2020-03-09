package gui;

import connection.ConnectionHandler;

import javax.swing.*;
import java.awt.*;

public class InitialConnectionWindow extends JFrame {

    public InitialConnectionWindow(ConnectionHandler connectionHandler) {

        super("Battleships");
        setLayout(new BorderLayout());
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel panel = setUpWindowContent(connectionHandler);
        add(panel);

        setVisible(true);
    }

    private JPanel setUpWindowContent(ConnectionHandler connectionHandler){

       JPanel buttonPanel = new JPanel();

        JButton connectToRemoteServerBtn = new JButton("Connect to remote server");
        buttonPanel.add(connectToRemoteServerBtn);

        connectToRemoteServerBtn.addActionListener(e -> {
            connectionHandler.initializeConnection(false);
            setVisible(false);
        });

        JButton createServerBtn = new JButton("Create server");
        buttonPanel.add(createServerBtn);
        createServerBtn.addActionListener(e -> {
            connectionHandler.initializeConnection(true);
            setVisible(false);
        });

        return buttonPanel;
    }


}
