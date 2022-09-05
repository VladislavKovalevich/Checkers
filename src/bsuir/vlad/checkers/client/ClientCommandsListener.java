
package bsuir.vlad.checkers.client;


import bsuir.vlad.checkers.commands.AbstractCheckerCommand;

public interface ClientCommandsListener {


    void onCommand(AbstractCheckerCommand command);

}
