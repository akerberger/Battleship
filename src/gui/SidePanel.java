package gui;

import javax.swing.*;
import java.awt.*;

public class SidePanel extends JPanel {


    private NorthPanel northPanel = new NorthPanel();
    private SouthPanel southPanel= new SouthPanel();

    public SidePanel() {
        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    private class NorthPanel extends JPanel{
        JLabel northLabel = new JLabel("Waiting...");

        public NorthPanel(){
            add(northLabel);
        }

        void setLabelText(String text){
            northLabel.setText(text);
        }
    }

    private class SouthPanel extends JPanel{

        JTextArea gameInstructionText = new JTextArea("");
        JTextArea shipDescriptionText = new JTextArea("*Ship description*");


        public SouthPanel(){
            setLayout(new GridLayout(2,1));
            gameInstructionText.setEditable(false);
            shipDescriptionText.setEditable(false);
            add(gameInstructionText);
//            add(Box.createRigidArea(new Dimension(1,5)));
            add(shipDescriptionText);
        }
        void setLabelText(String text){
            shipDescriptionText.setText(text);
        }
        void setGameInstructionText(String text){
            gameInstructionText.setText(text);
        }

        void setShipDescriptionText(String text){
            shipDescriptionText.setText(text);
        }
    }

    public void setupPhase(){
        northPanel.setLabelText("SETUP PHASE!");
        southPanel.setShipDescriptionText("Ship: Cruiser - \n3 squares horizontally");
        southPanel.setGameInstructionText("<- Klick on the square \nof your board where \nyour ship should be placed");
    }

    public void gamePhase(){
        northPanel.setLabelText("GAME PHASE!");
        southPanel.setShipDescriptionText("");
        southPanel.setGameInstructionText("Klick on a square \nof opponents board where \nyou want to shoot");
    }


    public void setLabelText(String text) {
        northPanel.setLabelText(text);
    }
}
