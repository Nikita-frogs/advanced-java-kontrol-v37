package exceptions;

public class ShipmentConfirmationException extends RuntimeException {
    public ShipmentConfirmationException(String message, Throwable cause) {
        super(message, cause);
    }
}
