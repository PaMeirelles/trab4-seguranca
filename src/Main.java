public class Main {
    public DatabaseManager databaseManager;

    public static void main(String[] args) throws Exception, RepeatingCharactersException {
        Main main = new Main();

        boolean isFirstAccess = DatabaseManager.isFirstAccess();
        Register r = new Register();
        if(isFirstAccess){
            r.registerAdmin();
        }
        else{
            if(!r.validateAdmin()){
                //TODO
                return;
            }
        }
    }
}