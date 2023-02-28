
public class InvalidInputException extends Exception{
    private final String message;

    @Override
    public String getMessage() {
        return message;
    }

    public InvalidInputException(String partWrong) {
        super(partWrong + " field contains an invalid entry.");
        this.message = partWrong + " field contains an invalid entry.";
    }
}
