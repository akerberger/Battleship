package gui.gamewindow;

import javax.swing.*;
import java.awt.*;

/**
 * Holds two panels with information labels that displays game information
 *   northPanel
 *       - gamePhaseText: information about current game phase
 *       - setupPhaseInstructionText: information about what the player should do at the current game phase
 *   southPanel
 *       - setupPhaseInstructionText: information about what the player should do at the setupPhase.
 *       - shipDescriptionText: information about the ship about to be placed during setup phase
 *       - shipDirectionText: information about the direction of the ship about to be placed during setup phase
 */
public class SidePanel extends JPanel {

    private NorthPanel northPanel = new NorthPanel();
    private SouthPanel southPanel= new SouthPanel();

    public SidePanel() {
        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    private class NorthPanel extends JPanel{
        JTextArea gamePhaseText = new JTextArea("Connection phase");
        JTextArea gameInstructionText = new JTextArea("Waiting for opponent\nto connect...");

        //Set up the north panel
        public NorthPanel(){
            setLayout(new GridLayout(2,1, 0,20));
            gamePhaseText.setEditable(false);
            gameInstructionText.setEditable(false);
            add(gamePhaseText);
            add(gameInstructionText);
        }

        void setGamePhaseText(String text){
            gamePhaseText.setText(text);
        }

        void setGameInstructionText(String text){
            gameInstructionText.setText(text);
        }
    }

    private class SouthPanel extends JPanel{

        JTextArea setupPhaseInstructionText = new JTextArea("");
        JTextArea shipDescriptionText = new JTextArea("");
        JLabel shipDirectionText = new JLabel("");

        //Set up the south panel
        public SouthPanel(){
            setLayout(new GridLayout(3,1, 0, 20));
            setupPhaseInstructionText.setEditable(false);
            shipDescriptionText.setEditable(false);
            add(setupPhaseInstructionText);
            add(shipDescriptionText);
            add(shipDirectionText);
        }

        void setSetupPhaseInstructionText(String text){
            setupPhaseInstructionText.setText(text);
        }
        void setShipDirectionText(String text){shipDirectionText.setText(text);}

        void setShipDescriptionText(String text){
            shipDescriptionText.setText(text);
        }
    }


    /**
     * Actions to take when the game phase has changed to setup phase
     */
    public void setupPhase(){
        northPanel.setGamePhaseText("Setup phase");
        northPanel.setGameInstructionText("Place 3 ships on your board\nthen wait for opponent\nto get ready...");
        southPanel.setShipDescriptionText("Ship: Cruiser - \n3 squares");
        southPanel.setShipDirectionText("Ship direction: Horizontal");
        southPanel.setSetupPhaseInstructionText("<- Klick on the square \nof your board where \nyour ship should be placed");
    }

    /**
     * Actions to take when the game phase has changed to setup phase
     * @param iGoFirst True if the player viewing this SidePanel object starts the game
     */
    public void gamePhase(boolean iGoFirst){
        northPanel.setGamePhaseText("Game phase");
        southPanel.setShipDescriptionText("");
        southPanel.setShipDirectionText("");
        southPanel.setSetupPhaseInstructionText("");
        northPanel.setGameInstructionText((iGoFirst ? "My turn!\nKlick on a square \nof opponents board where \nyou want to shoot":"Opponents turn..."));
    }

    /**
     * Actions to take when game over
     * @param whoWonText Information about which player won the game
     */
    public void gameOver(String whoWonText){
        setGamePhaseText("Game over");
        northPanel.setGameInstructionText(whoWonText);

    }

    /**
     * Update the gameInstructionText label with information of which of the players turn it is
     * @param wasMyTurn If the previous turn was that of the player viewing this SidePanel object
     */
    public void gameInstructionTextForNewTurn(boolean wasMyTurn){
        northPanel.setGameInstructionText((wasMyTurn ? "Opponents turn...":"My turn!\nKlick on a square \nof opponents board where \nyou want to shoot"));
    }

    /**
     * Update the shipDirectionText label
     * @param text The new text
     */
    public void setShipDirectionLabelText(String text){
        southPanel.setShipDirectionText(text);

    }

    /**
     * Update the gamePhaseText label
     * @param text The new text
     */
    public void setGamePhaseText(String text) {
        northPanel.setGamePhaseText(text);
    }
}
