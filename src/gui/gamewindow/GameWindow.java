package gui.gamewindow;

import client.BattleshipClient;
import gamecomponents.ShipPlacementOrientation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.net.InetAddress;

/**
 * The game window belonging to a BattleshipClient playing a Battleships game.
 * Receives/delivers game messages to/from the two playing boards. Tells the side panel what
 * game information to display
 */
public class GameWindow extends JFrame {

    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 700;

    private final BattleshipClient THE_CLIENT;

    private final SidePanel SIDE_PANEL = new SidePanel();
    private final PlayingBoard OWN_BOARD = new PlayingBoard(this, true);
    private final PlayingBoard OPPONENTS_BOARD = new PlayingBoard(this, false);

    //Handles the icons to be displayed on a game Square
    private final SquareIconHandler SQUARE_ICON_HANDLER = new SquareIconHandler();

    /**
     * Builds the GameWindow with all it's content
     *
     * @param client    The BattleshipClient that uses this GameWindow to play a game
     * @param hostName  The InetAddress of the host of the game session
     * @param port      The port number which the host of the game session uses for the game.
     * @param isHosting True if client is hosting the game
     */
    public GameWindow(BattleshipClient client, InetAddress hostName, int port, boolean isHosting) {
        super((isHosting ? "Hosting game at " : "Connected to game at ") +
                hostName.getHostAddress() + ", through port " + port);
        this.THE_CLIENT = client;

        setLayout(new BorderLayout());
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        setUpWindowContent();

        setVisible(true);

    }

    /**
     * Handles the icons of the squares. The icons indicate what type of game event has occurred on a Square.
     */
    private class SquareIconHandler {

        private final ImageIcon HIT_ON_OPPONENTS_BOARD_ICON = new ImageIcon(getClass().getClassLoader().getResource("redcross.png"));
        private final ImageIcon MISS_ON_OPPONENTS_BOARD_ICON = new ImageIcon(getClass().getClassLoader().getResource("bluecross.png"));
        private final ImageIcon HIT_ON_MY_BOARD_ICON = new ImageIcon(getClass().getClassLoader().getResource("redbackground.png"));
        private final ImageIcon MISS_ON_MY_BOARD_ICON = new ImageIcon(getClass().getClassLoader().getResource("bluebackground.png"));
        private final ImageIcon SUNKEN_SHIP_SQUARE_ICON = new ImageIcon(getClass().getClassLoader().getResource("skull.png"));

        ImageIcon getIcon(String iconType) {

            switch (iconType) {
                case "HIT_ON_OPPONENTS_BOARD_ICON":
                    return HIT_ON_OPPONENTS_BOARD_ICON;
                case "MISS_ON_OPPONENTS_BOARD_ICON":
                    return MISS_ON_OPPONENTS_BOARD_ICON;
                case "HIT_ON_MY_BOARD_ICON":
                    return HIT_ON_MY_BOARD_ICON;
                case "MISS_ON_MY_BOARD_ICON":
                    return MISS_ON_MY_BOARD_ICON;
                case "SUNKEN_SHIP_SQUARE_ICON":
                    return SUNKEN_SHIP_SQUARE_ICON;

            }
            return null;
        }
    }

    private void setUpWindowContent() {
        JPanel windowContentPanel = new JPanel();
        windowContentPanel.setLayout(new BorderLayout());

        windowContentPanel.add(OPPONENTS_BOARD, BorderLayout.NORTH);
        windowContentPanel.add(OWN_BOARD, BorderLayout.SOUTH);

        add(windowContentPanel, BorderLayout.WEST);
        add(SIDE_PANEL, BorderLayout.EAST);
    }

    /**
     * Sends a click event to the client of this GameWindow.
     *
     * @param row    The row of the PlayingBoard where the click occurred
     * @param column The column of the PlayingBoard where the click occurred
     */
    void sendClick(int row, int column) {
        THE_CLIENT.sendClick(row, column);
    }

    /**
     * Actions to take on game over.
     *
     * @param isWin True if the client of this GameWindow has won.
     */
    public void gameOver(boolean isWin) {
        String gameOverText = isWin ? "You win!" : "You loose!";

        SIDE_PANEL.gameOver(gameOverText);

        JOptionPane.showMessageDialog(this, "Game over! " + gameOverText);
    }


    /**
     * On the event of the opponent disconnects, makes the PlayingBoards un-clickable by removing the click listeners
     * and displays a pop-up screen informing the user that the opponent has disconnected.
     */
    public void onOpponentDisconnect() {
        OWN_BOARD.removeSquareListeners();
        OPPONENTS_BOARD.removeSquareListeners();
        JOptionPane.showMessageDialog(this, "Opponent disconnected!");
    }

    /**
     * Displays an information message in case of the BattleshipClient has been inactive longer than the
     * time out limit of the BattleshipClients Socket.
     */
    public void socketTimedOut() {
        JOptionPane.showMessageDialog(this, "You have been inactive too long! Restart program to start new game!");
    }

    /**
     * Actions for marking a Square as being shot, either as a hit or a miss, on the own board or on the opponents board
     * depending on which player made the shot.
     *
     * @param row      The row where the shot is to be marked on the PlayingBoard.
     * @param column   The column where the shot is to be marked on the PlayingBoards.
     * @param isMyShot True if the client of this GameWindow was the one that made the shot.
     * @param isHit    True if the shot is to be marked a hit.
     */
    public void markShot(int row, int column, boolean isMyShot, boolean isHit) {

        if (isMyShot) {

            OPPONENTS_BOARD.markShot(row, column, SQUARE_ICON_HANDLER.getIcon(isHit ? "HIT_ON_OPPONENTS_BOARD_ICON" : "MISS_ON_OPPONENTS_BOARD_ICON"));

        } else {

            OWN_BOARD.markShot(row, column, SQUARE_ICON_HANDLER.getIcon(isHit ? "HIT_ON_MY_BOARD_ICON" : "MISS_ON_MY_BOARD_ICON"));

        }
    }

    /**
     * Actions for marking a Square as containing part of a sunken ship.
     *
     * @param row
     * @param column
     * @param onOpponentsBoard
     */
    public void markSunkenShipSquare(int row, int column, boolean onOpponentsBoard) {
        if (onOpponentsBoard) {
            OPPONENTS_BOARD.markSunkenShipSquare(row, column, SQUARE_ICON_HANDLER.getIcon("SUNKEN_SHIP_SQUARE_ICON"));
        } else {
            OWN_BOARD.markSunkenShipSquare(row, column, SQUARE_ICON_HANDLER.getIcon("SUNKEN_SHIP_SQUARE_ICON"));
        }
    }

    public void setupPhase() {
        SIDE_PANEL.setupPhase();
        addShipPlacementListener();
        OWN_BOARD.addSquareListeners();
    }

    private void addShipPlacementListener() {

        OWN_BOARD.requestFocus();

        InputMap inputMap = OWN_BOARD.getInputMap();
        ActionMap actionMap = OWN_BOARD.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "turnShip");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "turnShip");
        actionMap.put("turnShip", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                THE_CLIENT.switchShipPlacementDirection();
            }
        });


    }

    private void removeShipPlacementListener() {
        OWN_BOARD.requestFocus();
        OWN_BOARD.getActionMap().remove("turnShip");
    }

    public void updateShipPlacementDirection(boolean horizontal) {
        SIDE_PANEL.setShipDirectionLabelText("Ship direction: " + (horizontal ? "horizontal" : "vertical"));
    }

    public void setNewTurnInfo(boolean wasMyTurn) {

        SIDE_PANEL.gameInstructionTextForNewTurn(wasMyTurn);
    }

    public void gamePhase(boolean iGoFirst) {
        SIDE_PANEL.gamePhase(iGoFirst);
        removeShipPlacementListener();
        if (iGoFirst) {
            OPPONENTS_BOARD.addSquareListeners();

        }
    }

    //detta är motsvarande receive click
    public void placeShipOnBoard(int startRow, int startColumn, int shipSize, ShipPlacementOrientation orientation) {
        OWN_BOARD.placeShipOnMyBoard(startRow, startColumn, shipSize, orientation);
    }

    public void addMouseListeners(boolean toOwnBoard) {

        if (toOwnBoard) {
            OWN_BOARD.addSquareListeners();
        } else {
            OPPONENTS_BOARD.addSquareListeners();
        }
    }


}
