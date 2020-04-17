package gui.gamewindow;

import client.BattleshipClient;
import gamecomponents.ShipPlacementOrientation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class GameWindow extends JFrame {

    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 700;

    public static final double PLAYING_BOARD_SIZE = WINDOW_WIDTH * 0.65;

//    private final BattleshipServer server;

    private final BattleshipClient client;

    private final SidePanel SIDE_PANEL = new SidePanel();

    private final PlayingBoard own = new PlayingBoard(this, true);
    private final PlayingBoard opponents = new PlayingBoard(this, false);

    //    public GameWindow(BattleshipServer server) {
    public GameWindow(BattleshipClient client, String hostName, int port, boolean isHosting) {
        super((isHosting ? "Hosting game at " : "Connected to game at ") +
                hostName + ", through port " + port);

        this.client = client;
        setLayout(new BorderLayout());
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        setUpWindowComponents();

        setVisible(true);

    }

    private void setUpWindowComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
//
//        own.setPreferredSize(new Dimension((int) PLAYING_BOARD_SIZE, (int) PLAYING_BOARD_SIZE));
//        own.setPreferredSize(new Dimension((int) PLAYING_BOARD_SIZE, (int) PLAYING_BOARD_SIZE));

        panel.add(opponents, BorderLayout.NORTH);
        panel.add(own, BorderLayout.SOUTH);

        add(panel, BorderLayout.WEST);
        add(SIDE_PANEL, BorderLayout.EAST);
    }

    public void sendClick(int row, int column) {
        client.sendClick(row, column);
    }


    //coordinates[0] = x
    //coordinates[1] = y
    public void receiveClick(String[] coordinates) {

        SIDE_PANEL.setLabelText("mottaget: " + coordinates[0] + " " + coordinates[1]);
    }

    public void setSidePanelText(String text) {
        SIDE_PANEL.setLabelText(text);
    }

    public void gameOver(boolean isWin){
        if(isWin){
            SIDE_PANEL.setLabelText("YOU WIN!");
        }else{
            SIDE_PANEL.setLabelText("YOU LOOSE!");
        }
    }


    public void markShot(int row, int column, boolean isMyClick, boolean isHit) {

        if (isMyClick) {
            if(isHit){
                opponents.markShot(row, column, isHit, new ImageIcon("/Users/Erik/IdeaProjects/Battleships/src/resources/redcross.png"));
            }else{
                opponents.markShot(row, column, isHit, new ImageIcon("/Users/Erik/IdeaProjects/Battleships/src/resources/bluecross.png"));
            }

        } else {
            if(isHit){
                own.markShot(row, column, isHit, new ImageIcon("/Users/Erik/IdeaProjects/Battleships/src/resources/redbackground.png"));
            }else{

                own.markShot(row, column, isHit, new ImageIcon("/Users/Erik/IdeaProjects/Battleships/src/resources/bluebackground.png"));
            }

        }
    }

    public void onOpponentDisconnect(){
        own.removeSquareListeners();
        opponents.removeSquareListeners();
        JOptionPane.showMessageDialog(this,"Opponent disconnected. Game Over (you win!)");
    }

    public void markSunkenShipSquare(int row, int column, boolean onOpponentsBoard) {
        if (onOpponentsBoard) {
            opponents.markSunkenShipSquare(row, column);
        } else {
            own.markSunkenShipSquare(row, column);
        }
    }

    public void setupPhase() {
        SIDE_PANEL.setupPhase();
        addShipPlacementListener();
        own.addSquareListeners();
    }

    private void addShipPlacementListener() {

//        addMouseListener(shipDirectionListener);

        own.requestFocus();

        InputMap inputMap = own.getInputMap();
        ActionMap actionMap = own.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "turnShip");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "turnShip");
        actionMap.put("turnShip", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("SVÄNGSKEPPE");
                client.switchShipPlacementDirection();
            }
        });


    }

    public void updateShipPlacementDirection(boolean horizontal) {
        SIDE_PANEL.setShipDirectionLabelText("Ship direction: " + (horizontal ? "horizontal" : "vertical"));
    }

    public void gamePhase(boolean iGoFirst) {
        SIDE_PANEL.gamePhase();

        if (iGoFirst) {
            opponents.addSquareListeners();
        }
    }

    //detta är motsvarande receive click
    public void placeShipOnBoard(int startRow, int startColumn, int shipSize, ShipPlacementOrientation orientation) {

        own.placeShipOnMyBoard(startRow, startColumn, shipSize, orientation);

        SIDE_PANEL.setLabelText("Opponent placing ship...");
    }

    public void addMouseListeners(boolean toOwnBoard) {

        if (toOwnBoard) {
            own.addSquareListeners();
        } else {
            opponents.addSquareListeners();
        }
    }


    //denna är onödig, markera bara samma sak på "mitt" bräde som på motståndarens,
    //det kommer bli samma koordinater
//	private int getColumnFromYCoordinate(int x) {
//
//		if (x > 0 && x <= PLAYING_BOARD_SIZE / 10) {
//			return 1;
//		} else if (x > PLAYING_BOARD_SIZE / 10 && x <= PLAYING_BOARD_SIZE / 10 * 2) {
//			return 2;
//		} else if (x > PLAYING_BOARD_SIZE / 10 * 2 && x <= PLAYING_BOARD_SIZE / 10 * 3) {
//			return 3;
//		} else if (x > PLAYING_BOARD_SIZE / 10 * 3 && x <= PLAYING_BOARD_SIZE / 10 * 4) {
//			return 4;
//		} else if (x > PLAYING_BOARD_SIZE / 10 * 4 && x <= PLAYING_BOARD_SIZE / 10 * 5) {
//			return 5;
//		} else if (x > PLAYING_BOARD_SIZE / 10 * 5 && x <= PLAYING_BOARD_SIZE / 10 * 6) {
//			return 6;
//		} else if (x > PLAYING_BOARD_SIZE / 10 * 6 && x <= PLAYING_BOARD_SIZE / 10 * 7) {
//			return 7;
//		} else if (x > PLAYING_BOARD_SIZE / 10 * 7 && x <= PLAYING_BOARD_SIZE / 10 * 8) {
//			return 8;
//		} else if (x > PLAYING_BOARD_SIZE / 10 * 8 && x <= PLAYING_BOARD_SIZE / 10 * 9) {
//			return 9;
//		} else if (x > PLAYING_BOARD_SIZE / 10 * 9 && x <= PLAYING_BOARD_SIZE / 10 * 10) {
//			return 10;
//		}
//
//		return -1;
//	}

}
