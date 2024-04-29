public class Register {
    public String pathCertificate;
    public String pathPrivateKey;
    public String secretPhrase;
    public Group group;
    public static String password;
    public static String confirmPassword;


    private static void checkInfo() throws InvalidPasswordFormatException, PasswordMismatchException {
        if (!password.matches("^[0-9]{8,10}$")) {
            throw new InvalidPasswordFormatException("Invalid password format: Password must be 8-10 digits long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match.");
        }

    }

    public static void registerAdmin(){
        return;
    }
    public static boolean validateAdmin(){
        return true;
    }
}
