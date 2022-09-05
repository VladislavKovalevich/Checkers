
package bsuir.vlad.checkers.server;


import bsuir.vlad.checkers.commands.AbstractCheckerCommand;
import bsuir.vlad.checkers.commands.CommandType;
import bsuir.vlad.checkers.commands.LogonCmd;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Player {

    private Socket socket;

    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    private String name;

    private boolean isLogon = false;

    private boolean isSubscribed = false;

    private boolean isConnected = false;


    public Player(Socket socket) {
        this.socket = socket;

        prepareSocket();
        checkLogon();
    }

    public boolean isLogon() {
        return isLogon;
    }

    private void prepareSocket() {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            disconnect();
        }
        isConnected = true;
    }

    private void checkLogon() {
        try {
            AbstractCheckerCommand command = readCommand();
            if ((command != null) && (command.getType().equals(CommandType.LogonCmd))) {
                LogonCmd logon = (LogonCmd)command;

                this.name = logon.getUserName();
                // check pass

                if (PlayersManager.getInstance().checkNewPlayer(this)) {
                    PlayersManager.getInstance().addPlayer(this);

                    System.out.println("player: " + getName() + " connected and logged in");

                    logon.setLoggedIn(true);
                    sendCommand(logon);

                    this.isLogon = true;
                } else {
                    System.out.println("player: " + getName() + " already logged in and active");

                    disconnect();
                }
            } else {
                disconnect();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            disconnect();
        }
    }

    private AbstractCheckerCommand readCommand() {
        try {
            return (AbstractCheckerCommand) objectInputStream.readUnshared();
        } catch (Exception ex) {
            ex.printStackTrace();
            disconnect();
        }

        return null;
    }

    public void sendCommand(AbstractCheckerCommand command) {
        try {
            if (isConnected()) {
                objectOutputStream.writeUnshared(command);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            disconnect();
        }
    }

    public boolean isConnected() {
        if (isConnected) {
            return true;
        } else {
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public void disconnect() {
        try {
            this.objectInputStream.close();
            this.objectOutputStream.close();
            this.socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.isLogon = false;
        this.isConnected = false;
    }

    public void subscribe(final CommandsListener listener) {
        this.isSubscribed = true;

        final Player player = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while ((isConnected()) && (isSubscribed)) {
                    AbstractCheckerCommand command = readCommand();

                    if (command != null) {
                        listener.onCommandEvent(new CommandEvent(player, command));
                    }
                }
            }
        }).start();
    }

    public void unsubscribe() {
        this.isSubscribed = false;
    }

}
