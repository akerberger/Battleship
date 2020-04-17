package gui;

import connection.ConnectionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class InitialConnectionWindow extends JFrame {

    private ConnectionHandler connectionHandler;

    private final String HOST_GAME_BTN_TXT = "Host local game";
    private final String JOIN_GAME_BTN_TXT = "Join local game";

    private class BtnListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            boolean hostGameBtnClicked = e.getActionCommand().equals(HOST_GAME_BTN_TXT);

            try {
                connectionHandler.initializeConnection(hostGameBtnClicked);
                InitialConnectionWindow.this.setVisible(false);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(InitialConnectionWindow.this,
                        hostGameBtnClicked ? "Local game is already running" :  "Could not find existing local game",
                        "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public InitialConnectionWindow(ConnectionHandler connectionHandler) {

        super("Battleships");
        this.connectionHandler = connectionHandler;

        setLayout(new BorderLayout());
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel panel = setUpWindowContent();
        add(panel);

        setVisible(true);
    }

    private JPanel setUpWindowContent() {

        JPanel buttonPanel = new JPanel();

        addConnectionButtons(buttonPanel);

        return buttonPanel;
    }

    private void addConnectionButtons(JPanel buttonPanel) {

        JButton connectToRemoteServerBtn = new JButton(JOIN_GAME_BTN_TXT);
        buttonPanel.add(connectToRemoteServerBtn);

        JButton createServerBtn = new JButton(HOST_GAME_BTN_TXT);
        buttonPanel.add(createServerBtn);

        BtnListener listener = new BtnListener();

        connectToRemoteServerBtn.addActionListener(listener);
        createServerBtn.addActionListener(listener);


    }


}
