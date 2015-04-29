package business.mocks;

public class MockUser {
    private String email, password;

    public MockUser(String email, String password) {
        this.password = password;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
