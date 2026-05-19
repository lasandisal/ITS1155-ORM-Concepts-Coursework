package lk.ijse.theserenitymentalhealththerapycenter.exception;

public class SessionScheduleException extends RuntimeException {
    public SessionScheduleException(String message) {
        super(message);
    }

    public SessionScheduleException(String message, Throwable cause) {
        super(message, cause);
    }
}
