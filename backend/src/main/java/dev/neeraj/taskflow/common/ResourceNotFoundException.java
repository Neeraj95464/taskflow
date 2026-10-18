package dev.neeraj.taskflow.common;

/** Thrown when a requested resource does not exist; rendered as HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super("%s with id %d not found".formatted(resource, id));
    }
}
