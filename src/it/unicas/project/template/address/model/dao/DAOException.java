package it.unicas.project.template.address.model.dao;

/**
 * Custom exception class used to wrap and abstract underlying data access errors.
 * <p>
 * This exception serves two primary purposes:
 * 1. **Abstraction:** It shields the business logic/service layer from specific technical exceptions
 * (e.g., SQLException, IOExceptions) that occur within the Data Access Object (DAO) layer.
 * 2. **Context:** It allows DAOs to add contextual information (via the message) about *what*
 * operation failed (e.g., "In select(): Connection refused") before rethrowing the error.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows any part of the application to catch this specific exception.
 * Inherits from {@code Exception} making it a checked exception, requiring explicit handling/declaration.
 */
public class DAOException extends Exception{

    /**
     * Constructor for DAOException.
     * <p>
     * Initializes the exception with a detailed message explaining the nature of the data access failure.
     * </p>
     *
     * @param message The detailed message about the data access error.
     */
    public DAOException(String message){
        super(message);
    }
}