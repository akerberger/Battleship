package gamecomponents.markers;

import javax.swing.*;
import java.awt.*;

public class Marker extends JPanel  {

    private ImageIcon icon;

    public Marker(){
        icon = new ImageIcon("/Users/Erik/IdeaProjects/Battleships/src/resources/skull.png");
    }


    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(icon.getImage(), 6,6,20,20,this);
    }

}
