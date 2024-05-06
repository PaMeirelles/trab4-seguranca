public class Main {
    public DatabaseManager databaseManager;

    public static void main(String[] args) {
        Main main = new Main();

        boolean isFirstAccess = DatabaseManager.isFirstAccess();
        Register r = new Register();
        if(isFirstAccess){
            r.registerAdmin();
        }
        else{
            if(!r.validateAdmin()){
                return;
            }
        }
    }
}