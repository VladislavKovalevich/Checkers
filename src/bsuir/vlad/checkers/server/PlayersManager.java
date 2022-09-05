
package bsuir.vlad.checkers.server;


import java.util.ArrayList;
import java.util.List;


public class PlayersManager {

    private static final PlayersManager instance = new PlayersManager();

    public static PlayersManager getInstance() {
        return instance;
    }

    private List<Player> players = new ArrayList<Player>();

    private PlayersManager() {
    }

    public void addPlayer(Player player) {
        // todo - remove players with same name but disconnected
        this.players.add(player);
    }

    public boolean checkNewPlayer(Player player) {
        for (Player pl : players) {
            if ((pl.getName().equals(player.getName())) && (pl.isConnected())) {
                return false;
            }
        }

        return true;
    }

    public Player getPlayerByName(String name) {
        for (Player player : players) {
            if (player.getName().equals(name) && (player.isConnected())) {
                return player;
            }
        }
        return null;
    }

}
