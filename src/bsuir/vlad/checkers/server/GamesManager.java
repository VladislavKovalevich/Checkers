
package bsuir.vlad.checkers.server;


import bsuir.vlad.checkers.commands.GameInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class GamesManager {

    private static final GamesManager instance = new GamesManager();

    public static GamesManager getInstance() {
        return instance;
    }

    private List<Game> games = new ArrayList<Game>();

    private GamesManager() {
    }

    public void addGame(final Game game) {
        games.add(game);
    }

    public List<GameInfo> getGamesList() {
        List<GameInfo> list = new ArrayList<>();

        for (Game game : games) {
            list.add(getGameInfo(game));
        }

        return list;
    }

    public Game getGameByID(final UUID gameID) {
        for (Game game : games) {
            if (game.getGameID().equals(gameID)) {
                return game;
            }
        }

        return null;
    }

    public GameInfo getGameInfo(Game game) {
        return new GameInfo(
                game.getGameID(),
                (game.getPlayer1() != null ? game.getPlayer1().getName() : null),
                (game.getPlayer2() != null ? game.getPlayer2().getName() : null),
                game.getStatus());
    }

}
