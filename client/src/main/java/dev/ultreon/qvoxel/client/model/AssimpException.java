package dev.ultreon.qvoxel.client.model;

public class AssimpException extends ModelException {
    public AssimpException() {
    }

    public AssimpException(String message) {
        super(message);
    }

    public AssimpException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssimpException(Throwable cause) {
        super(cause);
    }
}
