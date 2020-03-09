package main;

import connection.ConnectionHandler;
import gui.InitialConnectionWindow;

public class Main {

    public static void main(String [] args){
        new InitialConnectionWindow(new ConnectionHandler());
    }
}
