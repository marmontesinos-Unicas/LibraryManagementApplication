package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test Class for LoginService using Manual Stubbing (No Mockito).
 */
public class LoginServiceTest {

    private final String VALID_USERNAME = "testuser";
    private final String VALID_PASSWORD = "password123";
    private final String EXCEPTION_USERNAME = "erroruser";
    private final String EXCEPTION_MESSAGE = "Database connection timed out";
    private final User testUser = new User(1, "Test", "User", VALID_USERNAME, "12345678A", null, VALID_PASSWORD, "test@example.com", 1);


    /**
     * Inner class to act as a Test Stub for UserDAOMySQLImpl.
     * It overrides the getByUsername method to return pre-defined data.
     */
    private class TestUserDAOStub extends UserDAOMySQLImpl {
        // Note: Since UserDAOMySQLImpl is not an interface, we extend it.
        // We only need to override the method the LoginService calls.

        @Override
        public User getByUsername(String username) throws DAOException {
            if (username.equals(VALID_USERNAME)) {
                return testUser;
            } else if (username.equals(EXCEPTION_USERNAME)) {
                throw new DAOException(EXCEPTION_MESSAGE);
            } else {
                return null;
            }
        }
    }

    // Create the stub instance
    private final TestUserDAOStub userDAOStub = new TestUserDAOStub();

    // Instantiate the LoginService using the constructor meant for testing
    private final LoginService loginService = new LoginService(userDAOStub);


    @Test
    void authenticate_ValidCredentials_ReturnsUser() throws DAOException {
        // Arrange is done in the setup (passing the stub to the service)

        // Act
        User authenticatedUser = loginService.authenticate(VALID_USERNAME, VALID_PASSWORD);

        // Assert
        assertNotNull(authenticatedUser, "Authentication should succeed.");
        assertEquals(VALID_USERNAME, authenticatedUser.getUsername(), "The returned user should have the correct username.");
    }

    @Test
    void authenticate_IncorrectPassword_ReturnsNull() throws DAOException {
        // Arrange
        String INCORRECT_PASSWORD = "wrongpassword";

        // Act
        User authenticatedUser = loginService.authenticate(VALID_USERNAME, INCORRECT_PASSWORD);

        // Assert
        assertNull(authenticatedUser, "Authentication should fail due to incorrect password.");
    }

    @Test
    void authenticate_UserNotFound_ReturnsNull() throws DAOException {
        // Arrange
        String NON_EXISTENT_USERNAME = "nouser";
        // The stub is configured to return null for any username other than VALID_USERNAME or EXCEPTION_USERNAME

        // Act
        User authenticatedUser = loginService.authenticate(NON_EXISTENT_USERNAME, VALID_PASSWORD);

        // Assert
        assertNull(authenticatedUser, "Authentication should fail because the user does not exist.");
    }

    @Test
    void authenticate_DAOFails_ThrowsDAOException() {
        // Arrange: Use the special username configured in the stub to throw an exception

        // Act & Assert
        DAOException thrown = assertThrows(DAOException.class, () -> {
            loginService.authenticate(EXCEPTION_USERNAME, VALID_PASSWORD);
        }, "Should throw DAOException when DAO operation fails.");

        assertTrue(thrown.getMessage().contains(EXCEPTION_MESSAGE), "The exception message should match the one thrown by the stub.");
    }

    @Test
    void authenticate_NullOrEmptyInput_ReturnsNull() throws DAOException {
        // Act & Assert
        assertNull(loginService.authenticate(null, VALID_PASSWORD), "Should return null for null username.");
        assertNull(loginService.authenticate("", VALID_PASSWORD), "Should return null for empty username.");
        assertNull(loginService.authenticate(" ", VALID_PASSWORD), "Should return null for whitespace username.");
    }
}