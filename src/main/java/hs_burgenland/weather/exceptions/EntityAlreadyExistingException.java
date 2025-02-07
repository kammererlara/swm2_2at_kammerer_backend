package hs_burgenland.weather.exceptions;

public class EntityAlreadyExistingException extends Exception {
    public EntityAlreadyExistingException(final String message) {
        super(message);
    }
}
