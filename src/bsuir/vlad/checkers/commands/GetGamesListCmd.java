
package bsuir.vlad.checkers.commands;


import java.util.List;


public class GetGamesListCmd extends AbstractCheckerCommand {

    private List<GameInfo> gameInfos;

    public GetGamesListCmd() {
        super(CommandType.GetGamesListCmd);
    }

    public List<GameInfo> getGameInfos() {
        return gameInfos;
    }

    public void setGameInfos(List<GameInfo> gameInfos) {
        this.gameInfos = gameInfos;
    }

}
