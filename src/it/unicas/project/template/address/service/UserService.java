package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
    public void registerUser(User newUser) throws UserServiceException, DAOException {

        if (newUser.getName() == null || newUser.getName().trim().isEmpty()) {
            throw new UserServiceException("Error: Name is mandatory.");
        }
        if (newUser.getSurname() == null || newUser.getSurname().trim().isEmpty()) {
            throw new UserServiceException("Error: Surname is mandatory.");
        }
        if (newUser.getNationalID() == null || newUser.getNationalID().trim().isEmpty()) {
            throw new UserServiceException("Error: National ID is mandatory.");
        }
        if (newUser.getUsername() == null || newUser.getUsername().trim().isEmpty()) {
            throw new UserServiceException("Error: Username is mandatory.");
        }
        if (newUser.getPassword() == null || newUser.getPassword().trim().isEmpty()) {
            throw new UserServiceException("Error: Password is mandatory.");
        }

        // Validate Password Format: at least 8 characters, 1 uppercase, 1 digit
        String password = newUser.getPassword();
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new UserServiceException("Error: Password must be at least 8 characters long and include at least one uppercase letter and one number.");
        }

        // Validate Birth Date
        Object bdObj = newUser.getBirthdate();
        if (bdObj == null) {
            throw new UserServiceException("Error: User Birth Date is mandatory.");
        }
        if (bdObj instanceof LocalDate) {
            // OK: already a LocalDate
        } else {
            String bdStr = bdObj.toString();
            if (bdStr == null || bdStr.trim().isEmpty()) {
                throw new UserServiceException("Error: User Birth Date is mandatory.");
            }
            try {
                LocalDate.parse(bdStr.trim()); // expects yyyy-MM-dd (ISO)
            } catch (DateTimeParseException ex) {
                throw new UserServiceException("Error: User Birth Date must be in yyyy-MM-dd format.");
            }
        }

        // Check if the user already exists by National ID using the DAO's select method.
        User filter = new User(null, "", "", "", newUser.getNationalID(), null, "", "", null);
        if (!userDAO.select(filter).isEmpty()) {
            throw new UserServiceException("Error: The National ID is already registered in the system.");
        }

        // --- 3. Persistence ---
        userDAO.insert(newUser);
    }

    public UserService(it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl dao) {
        this.userDAO = dao;
    }
}
