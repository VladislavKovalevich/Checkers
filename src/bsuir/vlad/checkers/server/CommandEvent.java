
package bsuir.vlad.checkers.server;


import bsuir.vlad.checkers.commands.AbstractCheckerCommand;


public class CommandEvent {

    private final Player player;
    private final AbstractCheckerCommand command;

    public CommandEvent(final Player player, final AbstractCheckerCommand command) {
        this.player = player;
        this.command = command;
    }

    public Player getPlayer() {
        return player;
    }

    public AbstractCheckerCommand getCommand() {
        return command;
    }

}
