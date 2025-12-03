package it.unicas.project.template.address.service;

// Custom exception for business/validation errors
public class UserServiceException extends Exception {
    public UserServiceException(String message) {
        super(message);
    }
}