
package bsuir.vlad.checkers.commands;


public class LogonCmd extends AbstractCheckerCommand {

    private final String userName;
    private final String userPass;

    private boolean isLoggedIn = false;
    private String error;

    public LogonCmd(String name, String pass) {
        super(CommandType.LogonCmd);

        this.userName = name;
        this.userPass = pass;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPass() {
        return userPass;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
