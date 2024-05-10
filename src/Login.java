public class Login {
    public String login;
    public boolean loginStep1(String login) throws Exception {
        return !DatabaseManager.loginIsUnique(login);
    }
    public void login() throws Exception {
        if(!loginStep1(login)){
            // TODO
            return;
        }
        return;
    }
}
