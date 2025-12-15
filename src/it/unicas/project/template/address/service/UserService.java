package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class UserService {

    // Instance of the DAO (assuming Singleton pattern from your template)
    private UserDAOMySQLImpl userDAO = (UserDAOMySQLImpl) UserDAOMySQLImpl.getInstance();
    private LoanDAOMySQLImpl loanDAO = (LoanDAOMySQLImpl) LoanDAOMySQLImpl.getInstance();

    public UserService() {
        // userDAO is already initialized by the field initializer above.
    }

    public UserService(it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl dao) {
        this.userDAO = dao;
    }

    /**
     * Registers a new user in the system.
     */
    public void registerUser(User newUser) throws UserServiceException, DAOException {

        if (newUser.getName() == null || newUser.getName().trim().isEmpty()) {
            throw new UserServiceException("Error: Name is mandatory.");
        }
        if (newUser.getSurname() == null || newUser.getSurname().trim().isEmpty()) {
            throw new UserServiceException("Error: Surname is mandatory.");
        }
        if (newUser.getBirthdate() == null ) {
            throw new UserServiceException("Error: Birthdate is mandatory.");
        }
        if (newUser.getNationalID() == null || newUser.getNationalID().trim().isEmpty()) {
            throw new UserServiceException("Error: National ID is mandatory.");
        }
        if (newUser.getUsername() == null || newUser.getUsername().trim().isEmpty()) {
            throw new UserServiceException("Error: Username is mandatory.");
        }
        if (newUser.getPassword() == null || newUser.getPassword().trim().isEmpty()) {
            // Password mandatory for registration
            throw new UserServiceException("Error: Password is mandatory.");
        }
        if (newUser.getIdRole() == null || newUser.getIdRole().toString().trim().isEmpty()) {
            throw new UserServiceException("Error: Role is mandatory.");
        }

        // Validate Password Format: at least 8 characters, 1 uppercase, 1 digit
        String password = newUser.getPassword();
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            throw new UserServiceException("Error: Password must be at least 8 characters long and include at least one uppercase letter and one number.");
        }

        // Validate Birth Date
        Object bdObj = newUser.getBirthdate();
        if (bdObj == null) {
            throw new UserServiceException("Error: Birth Date is mandatory.");
        }
        if (!(bdObj instanceof LocalDate)) {
            String bdStr = bdObj.toString();
            if (bdStr == null || bdStr.trim().isEmpty()) {
                throw new UserServiceException("Error: Birth Date is mandatory.");
            }
            try {
                LocalDate.parse(bdStr.trim());
            } catch (DateTimeParseException ex) {
                throw new UserServiceException("Error: User Birth Date must be in yyyy-MM-dd format.");
            }
        }

        // Check if the user already exists by National ID AND Role ID.
        // The uniqueness rule is Role + NationalID.
        User filter = new User(null, "", "", "", newUser.getNationalID(), null, "", "", newUser.getIdRole());
        // Use the DAO's select method with the specific filter
        if (!userDAO.select(filter).isEmpty()) {
            throw new UserServiceException("Error: A user with the same National ID and Role is already registered in the system.");
        }

        // --- Persistence ---
        userDAO.insert(newUser);
    }

    // ----------------------------------------------------------------------
    // --- NEW METHODS FOR EDIT/DELETE FUNCTIONALITY ---
    // ----------------------------------------------------------------------

    /**
     * Updates an existing user in the system.
     * Reuses validation logic for mandatory fields, but handles password and uniqueness checks differently.
     */
    public void updateUser(User userToUpdate) throws UserServiceException, DAOException {

        // --- 1. Basic Validation (Mandatory fields) ---
        if (userToUpdate.getIdUser() == null || userToUpdate.getIdUser() <= 0) {
            throw new UserServiceException("Error: Cannot update user; ID is missing.");
        }
        if (userToUpdate.getName() == null || userToUpdate.getName().trim().isEmpty()) {
            throw new UserServiceException("Error: Name is mandatory.");
        }
        if (userToUpdate.getSurname() == null || userToUpdate.getSurname().trim().isEmpty()) {
            throw new UserServiceException("Error: Surname is mandatory.");
        }
        if (userToUpdate.getNationalID() == null || userToUpdate.getNationalID().trim().isEmpty()) {
            throw new UserServiceException("Error: National ID is mandatory.");
        }
        if (userToUpdate.getUsername() == null || userToUpdate.getUsername().trim().isEmpty()) {
            throw new UserServiceException("Error: Username is mandatory.");
        }
        if (userToUpdate.getBirthdate() == null) {
            throw new UserServiceException("Error: Birth Date is mandatory.");
        }
        if (userToUpdate.getIdRole() == null || userToUpdate.getIdRole() <= 0) {
            throw new UserServiceException("Error: Role is mandatory.");
        }

        // --- 2. Uniqueness Check (National ID + Role) ---
        // Check if the National ID is already taken by a different user
        User filterById = new User(null, "", "", "", userToUpdate.getNationalID(), null, "", "", userToUpdate.getIdRole());
        List<User> existingUsers = userDAO.select(filterById);

        if (!existingUsers.isEmpty()) {
            // If the combination is found, ensure it belongs to the user being updated
            for(User existing : existingUsers) {
                if (!existing.getIdUser().equals(userToUpdate.getIdUser())) {
                    throw new UserServiceException("Error: The National ID and Role combination is already used by another user.");
                }
            }
        }

        // --- 3. Password Check (Optional but must be valid if provided) ---
        String password = userToUpdate.getPassword();
        if (password != null && !password.trim().isEmpty()) {
            // Password must meet complexity requirements IF it is being updated
            if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
                throw new UserServiceException("Error: New password must be at least 8 characters long and include at least one uppercase letter and one number.");
            }
        }

        // --- 4. Persistence ---
        userDAO.update(userToUpdate); // Assuming your DAO has an update method.
    }

    /**
     * Deletes a user from the system by their ID.
     */
    public void deleteUser(User userToDelete) throws DAOException, UserServiceException {
        if (userToDelete.getIdUser() == null || userToDelete.getIdUser() <= 0) {
            throw new UserServiceException("Error: Cannot delete user; ID is missing.");
        }

        // --- Persistence ---
        userDAO.delete(userToDelete); // Assuming your DAO has a delete method.
    }

    // *** NEW METHOD: Checks for outstanding loans ***
    /**
     * Checks if the given user currently has any outstanding (unreturned) loans.
     * This method is used to enforce the business rule that a user cannot be deleted
     * if they have material yet to be returned.
     * @param user The user to check.
     * @return true if the user has active loans, false otherwise.
     * @throws DAOException if a database error occurs during the check.
     */
    public boolean hasOutstandingLoans(User user) throws DAOException {
        if (user == null || user.getIdUser() == null || user.getIdUser() <= 0) {
            return false;
        }

        // Use the new DAO method to check the database
        int activeLoanCount = loanDAO.countActiveLoansByUserId(user.getIdUser());

        return activeLoanCount > 0;
    }
}