
package bsuir.vlad.checkers.server;


import bsuir.vlad.checkers.commands.AbstractCheckerCommand;

import java.net.ServerSocket;
import java.net.Socket;


public class CheckersServer implements CommandsListener {

    private final int port;

    private ServerSocket serverSocket;


    public CheckersServer(int port) {
        this.port = port;
    }

    private void start() {
        try {
            this.serverSocket = new ServerSocket(this.port);

            while (true) {
                Socket socket = this.serverSocket.accept();

                connectClient(socket);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private synchronized void connectClient(Socket socket) {
        Player player = new Player(socket);

        if (player.isLogon()) {
            // client connected
            player.subscribe(this);

            System.out.println("player: " + player.getName() + " subscribed for commands");
        } else {
            // client disconnected
        }
    }

    @Override
    public synchronized void onCommandEvent(CommandEvent event) {
        AbstractCheckerCommand outCommand = CommandsProcessor.getInstance().processCommandEvent(event);

        if (outCommand != null) {
            event.getPlayer().sendCommand(outCommand);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: CheckersServer <port>");
            System.exit(-1);
        }

        int port = Integer.parseInt(args[0]);
        if (port < 0) {
            System.out.println("Wrong port");
            System.exit(-2);
        }

        new CheckersServer(port).start();
    }

}
