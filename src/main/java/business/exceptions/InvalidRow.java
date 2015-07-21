package business.exceptions;

public class InvalidRow extends RuntimeException {
    private static final long serialVersionUID = 4962263427071738791L;
    
    public InvalidRow(String message) {
        super(message);
    }
    
    public InvalidRow() {
        super("Invalid row.");
    }
}