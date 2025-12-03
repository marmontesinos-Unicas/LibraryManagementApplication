package it.unicas.project.template.address.service;

// Custom exception for business/validation errors
public class ServiceException extends Exception {
    public ServiceException(String message) {
        super(message);
    }
}