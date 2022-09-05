
package bsuir.vlad.checkers.server;


import bsuir.vlad.checkers.commands.*;

import java.util.List;


public class CommandsProcessor {

    private static final CommandsProcessor instance = new CommandsProcessor();

    public static CommandsProcessor getInstance() {
        return instance;
    }

    private CommandsProcessor() {
    }

    public AbstractCheckerCommand processCommandEvent(CommandEvent event) {

        if ((event == null) || (event.getPlayer() == null) || (event.getCommand() == null)) {
            return null;
        }

        switch (event.getCommand().getType()) {
            case CreateNewGameCmd:
                return processCreateNewGameCmd(event);
            case GetGamesListCmd:
                return processGetGamesListCmd(event);
            case JoinGameCmd:
                return processJoinGameCmd(event);
            case CheckersStatusCmd:
                return processStatusCmd(event);
            case CheckersTurnCmd:
                return processTurnCmd(event);
        }

        return null;
    }

    private AbstractCheckerCommand processCreateNewGameCmd(final CommandEvent event) {
        CreateNewGameCmd cmd = (CreateNewGameCmd) event.getCommand();

        Player player = PlayersManager.getInstance().getPlayerByName(cmd.getUserName());
        if (player == null) {
            System.out.println("Player not found.");
            cmd.setGameCreated(false);
            return cmd;
        }

        Game game = new Game();
        if (cmd.isWhite()) {
            game.setPlayer1(player);
        } else {
            game.setPlayer2(player);
        }
        game.setStatus(GameStatus.GameCreated);

        GamesManager.getInstance().addGame(game);

        cmd.setGameCreated(true);
        cmd.setGameInfo(GamesManager.getInstance().getGameInfo(game));

        return cmd;
    }

    private AbstractCheckerCommand processGetGamesListCmd(final CommandEvent event) {
        GetGamesListCmd cmd = (GetGamesListCmd) event.getCommand();

        List<GameInfo> list = GamesManager.getInstance().getGamesList();

        cmd.setGameInfos(list);

        return cmd;
    }

    private AbstractCheckerCommand processJoinGameCmd(final CommandEvent event) {
        JoinGameCmd cmd = (JoinGameCmd) event.getCommand();

        Player player = PlayersManager.getInstance().getPlayerByName(cmd.getUserName());
        Game game = GamesManager.getInstance().getGameByID(cmd.getGameID());

        if ((game == null) || (player == null)) {
            cmd.setGameJoined(false);
            return cmd;
        }

        if ((game.getPlayer1() != null) && (game.getPlayer1().getName().equals(player.getName()))) {
            if (!game.getPlayer1().isConnected()) {
                game.setPlayer1(player);
            }
        } else
        if ((game.getPlayer2() != null) && (game.getPlayer2().getName().equals(player.getName()))) {
            if (!game.getPlayer2().isConnected()) {
                game.setPlayer2(player);
            }
        } else
        if ((game.getPlayer1() == null) && (game.getPlayer2() != null) && (!game.getPlayer2().getName().equals(player.getName()))) {
            game.setPlayer1(player);
        } else
        if ((game.getPlayer2() == null) && (game.getPlayer1() != null) && (!game.getPlayer1().getName().equals(player.getName()))) {
            game.setPlayer2(player);
        }

        if ((game.getPlayer1() != null) && (game.getPlayer2() != null) &&
            (game.getPlayer1().isConnected()) && (game.getPlayer2().isConnected())) {
            game.setStatus(GameStatus.GameJoined);
        }
        cmd.setGameJoined(true);

        cmd.setGameInfo(GamesManager.getInstance().getGameInfo(game));

        return cmd;
    }

    private AbstractCheckerCommand processStatusCmd(final CommandEvent event) {
        CheckersStatusCmd statusCmd = (CheckersStatusCmd) event.getCommand();
        Game game = GamesManager.getInstance().getGameByID(statusCmd.getGameInfo().getGameID());

        if (statusCmd.getGameInfo().getStatus().equals(GameStatus.GameJoined)) {
            if ((game.getPlayer1() != null) && (game.getPlayer1().isConnected()) &&
                (game.getPlayer2() != null) && (game.getPlayer2().isConnected())) {
                game.setStatus(GameStatus.GameWhiteTurn);

                statusCmd = new CheckersStatusCmd(GamesManager.getInstance().getGameInfo(game));
                game.getPlayer1().sendCommand(statusCmd);
                game.getPlayer2().sendCommand(statusCmd);
            }
        }

        return null;
    }

    private AbstractCheckerCommand processTurnCmd(final CommandEvent event) {
        CheckersTurnCmd turnCmd = (CheckersTurnCmd) event.getCommand();
        Game game = GamesManager.getInstance().getGameByID(turnCmd.getGameID());

        if ((game.getStatus().equals(GameStatus.GameWhiteTurn)) && (turnCmd.isWhite())) {
            game.getTurns().add(new TurnPair());

            TurnPair pair = game.getTurns().get(game.getTurns().size() - 1);
            pair.setWhiteTurn(turnCmd.getTurn());
            game.setStatus(GameStatus.GameBlackTurn);

            CheckersStatusCmd statusCmd = new CheckersStatusCmd(GamesManager.getInstance().getGameInfo(game));
            game.getPlayer1().sendCommand(statusCmd);

            statusCmd.setPrevTurn(turnCmd.getTurn());
            game.getPlayer2().sendCommand(statusCmd);

        } else if ((game.getStatus().equals(GameStatus.GameBlackTurn)) && (!turnCmd.isWhite())) {
            TurnPair pair = game.getTurns().get(game.getTurns().size() - 1);
            pair.setBlackTurn(turnCmd.getTurn());
            game.setStatus(GameStatus.GameWhiteTurn);

            CheckersStatusCmd statusCmd = new CheckersStatusCmd(GamesManager.getInstance().getGameInfo(game));
            game.getPlayer2().sendCommand(statusCmd);

            statusCmd.setPrevTurn(turnCmd.getTurn());
            game.getPlayer1().sendCommand(statusCmd);
        }

        return null;
    }
}
