package business.exceptions;

public class EmailAddressNotUnique extends RuntimeException {
    private static final long serialVersionUID = 6789077965053928047L;
    public EmailAddressNotUnique(String message) {
        super(message);
    }
    public EmailAddressNotUnique() {
        super("Email address not available.");
    }
}