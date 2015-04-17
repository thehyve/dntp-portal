package business.representation;

public class NewPasswordRepresentation {
    private String password;
    private String token;

    public NewPasswordRepresentation() {}

    public NewPasswordRepresentation(String password, String token) {
        this.password = password;
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }
}
