package main;

import connection.ConnectionHandler;
import gui.InitialConnectionWindow;

//TODO: fixa texten för motståndaren när jag gör timeout. Testa hela spelet efter de senaste ändringarna! Den
//verkar t.ex. inte ta bort trådar från tråd-listan som den borde...
//trådar i listan innan: 2
//trådar i listan efter: 2
//(kanske iofs inte spelar nån roll men ändå)

public class Main {

    public static void main(String [] args){
        new InitialConnectionWindow(new ConnectionHandler());
    }
}
