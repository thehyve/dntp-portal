package business.representation;

public class PasswordChangeRepresentation {
    private String oldPassword;
    private String newPassword;

    public PasswordChangeRepresentation() {}
    public PasswordChangeRepresentation(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
