package com.acme.library.common;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " " + id + " not found");
    }
}
