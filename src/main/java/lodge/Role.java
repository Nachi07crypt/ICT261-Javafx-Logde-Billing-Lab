package lodge;

/** What kind of account a user has. */
public enum Role {
    GUEST("Guest"),
    ADMIN("Admin");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
