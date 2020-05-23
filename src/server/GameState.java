package server;

/**
 * The different phases of a game session
 */
public enum GameState {
    CONNECTION_PHASE, // Waiting for two players to connect
    SETUP_PHASE, //Two connected players. Waiting for the players to place all their ships on their playing board.
    GAME_PHASE, //Both players have placed their ships. The actual game phase where players take turns shooting.
    GAME_OVER //When a player has won
}
