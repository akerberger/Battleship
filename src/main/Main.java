package main;

import gameinitiation.GameInitiationHandler;
import gui.InitialConnectionWindow;
//TODO: Hashcode/equals i Square
/**
 * Entry point for the Battleships program
 */
public class Main {

    /**
     * Creates the initial window that is displayed when a user starts the program
     * @param args - unused
     */
    public static void main(String [] args){
        new InitialConnectionWindow(new GameInitiationHandler());
    }
}
