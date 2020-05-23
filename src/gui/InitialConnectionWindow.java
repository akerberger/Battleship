package gui;

import gameinitiation.GameInitiationHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * The initial window that is presented to a user when starting the Battleships program.
 */
public class InitialConnectionWindow extends JFrame {

    private GameInitiationHandler gameInitiationHandler;

    private final String HOST_GAME_BTN_TXT = "Host local game";
    private final String JOIN_GAME_BTN_TXT = "Join local game";

    /**
     * Builds the window
     * @param gameInitiationHandler Will be used to initiate a game
     */
    public InitialConnectionWindow(GameInitiationHandler gameInitiationHandler) {

        super("Battleships");
        this.gameInitiationHandler = gameInitiationHandler;

        setLayout(new BorderLayout());
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel panel = setUpWindowContentPanel();
        add(panel);

        setVisible(true);
    }

    private class BtnListener implements ActionListener {

        /**
         * Tries to, through the gameInitiationHandler object, initiate a new local game or connect to
         * an existing local game depending on which of the two buttons is pressed.
         *
         * Will display a popup if an exception has been thrown by the gameInitiationHandler informing the user
         * about:
         * - If trying to host, the exception indicates that a local game has already been initiated at the port number provided by the user at
         * program start (or the default port if no value was provided).
         * - If trying to join, the exception indicates that no local game is running at the port number provided by the user at program start
         * (or the default port if no value was provided).
         *
         * Closes the window if the connection/game initiation is successful.
         * @param e The ActionEvent object
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            boolean hostGameBtnClicked = e.getActionCommand().equals(HOST_GAME_BTN_TXT);

            try {
                gameInitiationHandler.handleGameInitiation(hostGameBtnClicked);
                InitialConnectionWindow.this.setVisible(false);
            }catch(NumberFormatException nfe){
                JOptionPane.showMessageDialog(InitialConnectionWindow.this,
                        "Could not start game! ",
                        "Error!", JOptionPane.ERROR_MESSAGE);
                nfe.printStackTrace();
            }
            catch (IOException ioe) {
                JOptionPane.showMessageDialog(InitialConnectionWindow.this,
                        hostGameBtnClicked ? "Local game is already running at port "+gameInitiationHandler.getPort() :  "Could not find existing local game on port "+gameInitiationHandler.getPort(),
                        "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private JPanel setUpWindowContentPanel() {

        JPanel buttonPanel = new JPanel();

        addConnectionButtons(buttonPanel);

        return buttonPanel;
    }

    /**
     * Creates and adds the two buttons, with listeners, handling the connection when clicked.
     * @param buttonPanel A container panel for the two buttons
     */
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
