
package bsuir.vlad.checkers.commands;


import java.io.Serializable;


public abstract class AbstractCheckerCommand implements Serializable {

    private final CommandType type;

    public AbstractCheckerCommand(CommandType type) {
        this.type = type;
    }

    public CommandType getType() {
        return type;
    }

}
