package gui.gamewindow;

import javax.swing.*;
import java.awt.*;

/**
 * Holds information labels:
 *   northPanel
 *       - 
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
        JTextArea northTextArea = new JTextArea("Waiting for opponent\nto connect...");

        JTextArea gameInstructionText = new JTextArea("");

        public NorthPanel(){
            setLayout(new GridLayout(2,1, 0,20));
            northTextArea.setEditable(false);
            gameInstructionText.setEditable(false);
            add(northTextArea);
            add(gameInstructionText);
        }

        void setText(String text){
            northTextArea.setText(text);
        }

        void setGameInstructionText(String text){
            gameInstructionText.setText(text);
        }
    }

    private class SouthPanel extends JPanel{

        JTextArea gameInstructionText = new JTextArea("");
        JTextArea shipDescriptionText = new JTextArea("");
        JLabel shipDirectionText = new JLabel("");


        public SouthPanel(){
            setLayout(new GridLayout(3,1, 0, 20));
            gameInstructionText.setEditable(false);
            shipDescriptionText.setEditable(false);
            add(gameInstructionText);
//            add(Box.createRigidArea(new Dimension(1,5)));
            add(shipDescriptionText);
            add(shipDirectionText);
        }
        void setLabelText(String text){
            shipDescriptionText.setText(text);
        }
        void setGameInstructionText(String text){
            gameInstructionText.setText(text);
        }
        void setShipDirectionText(String text){shipDirectionText.setText(text);}
        void setShipDescriptionText(String text){
            shipDescriptionText.setText(text);
        }
    }



    public void setupPhase(){
        northPanel.setText("SETUP PHASE!");
        northPanel.setGameInstructionText("Place 3 ships on your board\nthen wait for opponent\nto get ready...");
        southPanel.setShipDescriptionText("Ship: Cruiser - \n3 squares");
        southPanel.setShipDirectionText("Ship direction: Horizontal");
        southPanel.setGameInstructionText("<- Klick on the square \nof your board where \nyour ship should be placed");
    }

    public void gamePhase(boolean iGoFirst){
        northPanel.setText("GAME PHASE!");
        southPanel.setShipDescriptionText("");
        southPanel.setShipDirectionText("");
        southPanel.setGameInstructionText("");
        northPanel.setGameInstructionText((iGoFirst ? "My turn!\nKlick on a square \nof opponents board where \nyou want to shoot":"Opponents turn..."));
    }

    public void gameOver(String gameOverText){
        setLabelText("GAME OVER!");
        northPanel.setGameInstructionText(gameOverText);

    }

    public void gameInstructionTextForNewTurn(boolean wasMyTurn){
        northPanel.setGameInstructionText((wasMyTurn ? "Opponents turn...":"My turn!\nKlick on a square \nof opponents board where \nyou want to shoot"));
    }

    public void setShipDirectionLabelText(String text){
        southPanel.setShipDirectionText(text);

    }

    public void setLabelText(String text) {
        northPanel.setText(text);
    }
}
