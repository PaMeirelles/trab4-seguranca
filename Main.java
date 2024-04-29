public class Main {
    public DatabaseManager databaseManager;

    public static void main(String[] args) {
        Main main = new Main();

        boolean isFirstAccess = DatabaseManager.isFirstAccess();

        if(isFirstAccess){
            Register.register_admin();
        }
        else{
            if(!Register.validate_admin()){
                return;
            }
        }
    }
}