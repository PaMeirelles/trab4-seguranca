package Controller;

import Model.DatabaseManager;
import Model.Register;
import Model.RepeatingCharactersException;

public class Main {
    public static void main(String[] args) throws Exception {
        boolean isFirstAccess = DatabaseManager.isFirstAccess();
        Register r = new Register();
        if(isFirstAccess){
            r.registerAdmin();
        }
        else{
            if(!r.validateAdmin("")){
                //TODO
                return;
            }
        }
    }
}