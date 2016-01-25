package business.representation;

public class EmailRepresentation {
    private String email;

    public EmailRepresentation() {}

    public EmailRepresentation(String email) {
        this.email = email.toLowerCase();
    }

    public String getEmail() {
        return this.email;
    }
}
