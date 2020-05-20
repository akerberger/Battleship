package server;

//The different phases of a game session
public enum GameState {
    CONNECTION_PHASE, // Wait for two players to connect
    SETUP_PHASE, //Wait for the players to place all their ships on their playing board.
    GAME_PHASE, //The actual game phase where players take turns shooting.
    GAME_OVER //When a player has won
}
