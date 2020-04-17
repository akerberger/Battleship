package main;

import connection.ConnectionHandler;
import gui.InitialConnectionWindow;

//TODO: fixa så att den inte kraschar vid timeout, eller så att motståndaren meddelas vinst vid timeout

public class Main {

    public static void main(String [] args){
        new InitialConnectionWindow(new ConnectionHandler());
    }
}
