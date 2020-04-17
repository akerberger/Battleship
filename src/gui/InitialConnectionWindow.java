package gui;

import connection.ConnectionHandler;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

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

       addConnectionButtons(buttonPanel, connectionHandler);

        return buttonPanel;
    }

    private void addConnectionButtons(JPanel buttonPanel, ConnectionHandler connectionHandler){

        JButton connectToRemoteServerBtn = new JButton("Join local game");
        buttonPanel.add(connectToRemoteServerBtn);

        connectToRemoteServerBtn.addActionListener(e -> {
            boolean connectionSuccess = true;
            try{connectionHandler.initializeConnection(false);

            }catch(IOException ioe){
                connectionSuccess = false;
                System.out.println("hittade inget lokalt spel");
                JOptionPane.showMessageDialog(this,"Could not find existing local game", "Error!",JOptionPane.ERROR_MESSAGE);
            }
            //Om boolean...
            if(connectionSuccess){
                setVisible(false);
            }
        });

        JButton createServerBtn = new JButton("Host local game");
        buttonPanel.add(createServerBtn);
        createServerBtn.addActionListener(e -> {
            boolean connectionSuccess = true;
            try{connectionHandler.initializeConnection(true);

            }catch(IOException ioe){
                System.out.println("fel i göra egen server");
                connectionSuccess = false;
                JOptionPane.showMessageDialog(this,"Local game already exists. Why not join it?", "Error!",JOptionPane.ERROR_MESSAGE);
            }
            if(connectionSuccess){
                setVisible(false);
            }

        });

    }


}
