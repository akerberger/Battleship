package gui.gamewindow;

import client.BattleshipClient;
import gamecomponents.ShipPlacementOrientation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class GameWindow extends JFrame {

    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 700;

    private final BattleshipClient client;

    private final SidePanel SIDE_PANEL = new SidePanel();

    private final PlayingBoard own = new PlayingBoard(this, true);
    private final PlayingBoard opponents = new PlayingBoard(this, false);

    private final ImageIcon HIT_ON_OPPONENTS_BOARD_ICON = new ImageIcon(getClass().getClassLoader().getResource("redcross.png"));
    private final ImageIcon MISS_ON_OPPONENTS_BOARD_ICON= new ImageIcon(getClass().getClassLoader().getResource("bluecross.png"));
    private final ImageIcon HIT_ON_MY_BOARD_ICON= new ImageIcon(getClass().getClassLoader().getResource("redbackground.png"));
    private final ImageIcon MISS_ON_MY_BOARD_ICON= new ImageIcon(getClass().getClassLoader().getResource("bluebackground.png"));


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

        panel.add(opponents, BorderLayout.NORTH);
        panel.add(own, BorderLayout.SOUTH);

        add(panel, BorderLayout.WEST);
        add(SIDE_PANEL, BorderLayout.EAST);
    }

    public void sendClick(int row, int column) {
        client.sendClick(row, column);
    }


    public void gameOver(boolean isWin) {
        String gameOverText = isWin ? "You win!" : "You loose!";

        SIDE_PANEL.gameOver(gameOverText);

        JOptionPane.showMessageDialog(this, "Game over! " + gameOverText);
    }


    public void markShot(int row, int column, boolean isMyClick, boolean isHit) {

        if (isMyClick) {
            if (isHit) {
                opponents.markShot(row, column, isHit, HIT_ON_OPPONENTS_BOARD_ICON);
            } else {
                opponents.markShot(row, column, isHit, MISS_ON_OPPONENTS_BOARD_ICON);
            }

        } else {
            if (isHit) {
                own.markShot(row, column, isHit, HIT_ON_MY_BOARD_ICON);
            } else {

                own.markShot(row, column, isHit, MISS_ON_MY_BOARD_ICON);
            }

        }
    }

    public void onOpponentDisconnect() {
        own.removeSquareListeners();
        opponents.removeSquareListeners();
        JOptionPane.showMessageDialog(this, "Opponent disconnected!");
    }

    public void socketTimedOut() {
        JOptionPane.showMessageDialog(this, "You have been inactive too long! Restart program to start new game!");
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

    private void removeShipPlacementListener(){
        own.requestFocus();
        own.getActionMap().remove("turnShip");
    }

    public void updateShipPlacementDirection(boolean horizontal) {
        SIDE_PANEL.setShipDirectionLabelText("Ship direction: " + (horizontal ? "horizontal" : "vertical"));
    }

    public void setNewTurnInfo(boolean wasMyTurn){

//            SIDE_PANEL.setLabelText("GAME PHASE!\n"+(wasMyTurn ? "Opponents turn... ": "My turn!"));
            SIDE_PANEL.gameInstructionTextForNewTurn(wasMyTurn);
    }

    public void gamePhase(boolean iGoFirst) {
        SIDE_PANEL.gamePhase(iGoFirst);
        removeShipPlacementListener();
        if (iGoFirst) {
            opponents.addSquareListeners();

        }
    }

    //detta är motsvarande receive click
    public void placeShipOnBoard(int startRow, int startColumn, int shipSize, ShipPlacementOrientation orientation) {

        own.placeShipOnMyBoard(startRow, startColumn, shipSize, orientation);


    }

    public void addMouseListeners(boolean toOwnBoard) {

        if (toOwnBoard) {
            own.addSquareListeners();
        } else {
            opponents.addSquareListeners();
        }
    }




}
