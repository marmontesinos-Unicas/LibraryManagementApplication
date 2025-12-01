package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;

public class UserService {

    // Instance of the DAO (assuming Singleton pattern from your template)
    private UserDAOMySQLImpl userDAO = (UserDAOMySQLImpl) UserDAOMySQLImpl.getInstance();

    // Add a no-argument constructor so callers like "new UserService()" compile.
    public UserService() {
        // userDAO is already initialized by the field initializer above.
    }

    /**
     * Registers a new user in the system.
     * Acceptance Criteria 1 & 2: Validates required fields and ensures uniqueness.
     */
    public void registerUser(User newUser, String postalCode) throws ServiceException, DAOException {

        // --- 1. Required Field Validation (Acceptance Criteria 2) ---
        // Fields based on the User Story: name, surname, ID (nationalID), postal code

        if (newUser.getName() == null || newUser.getName().trim().isEmpty()) {
            throw new ServiceException("Error: User name is mandatory.");
        }
        if (newUser.getSurname() == null || newUser.getSurname().trim().isEmpty()) {
            throw new ServiceException("Error: User surname is mandatory.");
        }
        if (newUser.getNationalID() == null || newUser.getNationalID().trim().isEmpty()) {
            throw new ServiceException("Error: User National ID is mandatory.");
        }
        if (postalCode == null || postalCode.trim().isEmpty()) {
            // Note: Postal code validation is handled here since it is a requirement
            // but is not part of your existing User model class.
            throw new ServiceException("Error: Postal Code is mandatory.");
        }

        // --- 2. Uniqueness Validation ---
        // Check if the user already exists by National ID using the DAO's select method.
        User filter = new User(null, "", "", "", newUser.getNationalID(), "", "", null);

        if (!userDAO.select(filter).isEmpty()) {
            throw new ServiceException("Error: The National ID is already registered in the system.");
        }

        // --- 3. Persistence ---
        userDAO.insert(newUser);
    }

    public UserService(it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl dao) {
        this.userDAO = dao;
    }
}
