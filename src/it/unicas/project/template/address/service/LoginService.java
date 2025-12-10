package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;

/**
 * Service class responsible for handling user login logic.
 * It uses the UserDAOMySQLImpl to retrieve user data, injected via the constructor.
 */
public class LoginService {

    // The dependency is now a final field and will be set through the constructor.
    private final UserDAOMySQLImpl userDAO;

    /**
     * Constructor used for production code, relying on the static getInstance().
     * (We keep this for backward compatibility with your Controller)
     */
    public LoginService() {
        // Production: Use the singleton instance
        this(UserDAOMySQLImpl.getInstance());
    }

    /**
     * Constructor used for testing (Dependency Injection).
     * This is the key change to make the service testable without Mockito.
     * * @param userDAO The DAO implementation to use (can be a real one or a test stub).
     */
    public LoginService(UserDAOMySQLImpl userDAO) {
        this.userDAO = userDAO;
    }

    // ... (authenticate method remains the same)

    /**
     * Attempts to authenticate a user with the provided credentials.
     * * @param username The username provided by the user.
     * @param password The raw password provided by the user.
     * @return The authenticated User object if credentials are valid, or null otherwise.
     * @throws DAOException If there is an error accessing the database.
     */
    public User authenticate(String username, String password) throws DAOException {
        if (username == null || password == null || username.trim().isEmpty()) {
            return null;
        }

        User userFromDB = userDAO.getByUsername(username);

        if (userFromDB == null) {
            return null;
        }

        // Check password (simple comparison for this example)
        if (userFromDB.getPassword().equals(password)) {
            return userFromDB;
        } else {
            return null;
        }
    }
}