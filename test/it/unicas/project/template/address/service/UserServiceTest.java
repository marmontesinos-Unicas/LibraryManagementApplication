package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mock implementation of the UserDAOMySQLImpl for testing the UserService.
 * This class simulates database operations using an in-memory list and is stateful.
 */
class MockUserDAOImpl extends UserDAOMySQLImpl {
    private final List<User> database = new ArrayList<>();
    // Starts ID from 1 to simulate auto-increment
    private final AtomicInteger nextId = new AtomicInteger(1);

    public MockUserDAOImpl() {
        // Placeholder constructor
    }

    /**
     * Clears the in-memory database and resets the ID counter.
     */
    public void reset() {
        database.clear();
        nextId.set(1);
    }

    // --- Mock DAO Methods ---

    @Override
    public List<User> select(User u) throws DAOException {
        // If the filter is null, return all users (for verification purposes)
        if (u == null) return new ArrayList<>(database);

        // Simulate selection based on NationalID (used for uniqueness check)
        return database.stream()
                .filter(user -> {
                    boolean nationalIDMatch = u.getNationalID() != null && !u.getNationalID().isEmpty() && user.getNationalID().equals(u.getNationalID());
                    return nationalIDMatch;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void insert(User u) throws DAOException {
        // Assign a mock ID and add to the list
        User newUser = new User(
                nextId.getAndIncrement(),
                u.getName(), u.getSurname(), u.getUsername(), u.getNationalID(),
                u.getBirthdate(), u.getPassword(), u.getEmail(), u.getIdRole()
        );
        database.add(newUser);
    }

    @Override
    public void update(User u) throws DAOException {
        // Find the user by ID and replace it with the new object
        for (int i = 0; i < database.size(); i++) {
            if (database.get(i).getIdUser().equals(u.getIdUser())) {
                database.set(i, u);
                return;
            }
        }
        throw new DAOException("User not found for update: ID " + u.getIdUser());
    }

    @Override
    public void delete(User u) throws DAOException {
        // Remove the user by ID
        boolean removed = database.removeIf(user -> user.getIdUser().equals(u.getIdUser()));
        if (!removed) {
            throw new DAOException("User not found for deletion: ID " + u.getIdUser());
        }
    }
}

/**
 * Tests for the UserService class using a custom Mock DAO implementation.
 */
public class UserServiceTest {

    private UserService userService;
    private MockUserDAOImpl mockDAO;
    private final LocalDate birthday = LocalDate.of(2002, 10, 20);

    @BeforeEach
    public void setUp() {
        // 1. Initialize the Mock DAO and reset its state before each test
        mockDAO = new MockUserDAOImpl();
        mockDAO.reset();

        // 2. Inject the Mock DAO into the UserService
        // Note: This relies on the UserService(UserDAOMySQLImpl) constructor you created earlier.
        userService = new UserService(mockDAO);
    }

    // --- Helper Methods to Create User Objects ---

    private User createValidUser(String nationalID) {
        return new User(
                null, // idUser is null for new registration
                "TestName",
                "TestSurname",
                "TestUsername",
                nationalID,
                birthday,
                "StrongP@ss1", // Valid password
                "test." + nationalID + "@example.com",
                2 // User role (2 for 'User')
        );
    }

    private User createValidExistingUser(Integer id, String nationalID) {
        return new User(
                id,
                "Existing",
                "User",
                "existuser",
                nationalID,
                LocalDate.of(1980, 1, 1),
                "ExistingP@ss1",
                "e@e.com",
                1 // Admin role (1 for 'Admin')
        );
    }

    /**
     * Creates a deep copy of a User object. Essential for update tests
     * to avoid modifying the object reference stored in the Mock DAO's list.
     */
    private User createUserCopy(User original) {
        return new User(
                original.getIdUser(),
                original.getName(),
                original.getSurname(),
                original.getUsername(),
                original.getNationalID(),
                original.getBirthdate(),
                original.getPassword(),
                original.getEmail(),
                original.getIdRole()
        );
    }


    // --------------------------------------------------------------------------
    //                         REGISTRATION TESTS (UNCHANGED LOGIC)
    // --------------------------------------------------------------------------

    @Test
    public void testRegisterUserSuccess() throws Exception {
        User newUser = createValidUser("12345678X");
        newUser.setIdRole(2); // Ensure role is set for validation

        // Act
        userService.registerUser(newUser);

        // Assert
        List<User> usersInDb = mockDAO.select(null);
        assertEquals(1, usersInDb.size(), "User count should be 1 after successful registration.");
        assertEquals("12345678X", usersInDb.get(0).getNationalID());
        assertEquals("TestName", usersInDb.get(0).getName());
    }

    @Test
    public void testRegisterUserMissingNameThrowsServiceException() {
        User newUser = createValidUser("111");
        newUser.setName(""); // Set name to empty string
        newUser.setIdRole(2);

        Exception ex = assertThrows(UserServiceException.class, () -> userService.registerUser(newUser));
        assertTrue(ex.getMessage().contains("Name is mandatory"));
    }

    @Test
    public void testRegisterUserDuplicateNationalIdThrowsServiceException() throws DAOException {
        // Setup: Pre-populate the mock DB with an existing user
        mockDAO.insert(createValidExistingUser(null, "NID123"));

        User newUser = createValidUser("NID123"); // New user with the same ID
        newUser.setIdRole(2);

        // Act & Assert
        Exception ex = assertThrows(UserServiceException.class, () -> userService.registerUser(newUser));
        assertTrue(ex.getMessage().contains("National ID is already registered"));
        assertEquals(1, mockDAO.select(null).size(), "Only one user should remain in the DB.");
    }

    @Test
    public void testRegisterUserInvalidPasswordThrowsServiceException() {
        User newUser = createValidUser("NID999");
        newUser.setPassword("password"); // Invalid: missing uppercase/digit
        newUser.setIdRole(2);

        Exception ex = assertThrows(UserServiceException.class, () -> userService.registerUser(newUser));
        assertTrue(ex.getMessage().contains("Password must be at least 8 characters"));
    }

    // --------------------------------------------------------------------------
    //                             NEW UPDATE TESTS
    // --------------------------------------------------------------------------

    @Test
    void testUpdateUser_Success_NoPasswordChange() throws Exception {
        // Setup: Insert user (ID=1)
        User initialUser = createValidExistingUser(null, "444");
        mockDAO.insert(initialUser);
        User originalUser = mockDAO.select(null).get(0); // Get user with assigned ID (ID=1)

        // Create a copy for the update
        User userToUpdate = createUserCopy(originalUser);

        // Arrange: Make a change but keep password null/empty
        userToUpdate.setSurname("NewSurname");
        userToUpdate.setPassword(null);

        // Act
        userService.updateUser(userToUpdate);

        // Assert
        User updatedUser = mockDAO.select(null).get(0);
        assertEquals("NewSurname", updatedUser.getSurname());
    }

    @Test
    void testUpdateUser_Success_WithNewValidPassword() throws Exception {
        // Setup: Insert user (ID=1)
        mockDAO.insert(createValidExistingUser(null, "555"));
        User originalUser = mockDAO.select(null).get(0);

        // Create a copy for the update
        User userToUpdate = createUserCopy(originalUser);

        String newPassword = "ValidN3wP@ss1";
        userToUpdate.setPassword(newPassword);

        // Act
        userService.updateUser(userToUpdate);

        // Assert
        User updatedUser = mockDAO.select(null).get(0);
        assertEquals(newPassword, updatedUser.getPassword());
    }

    @Test
    void testUpdateUser_Failure_MissingSurname() throws DAOException {
        // Setup: Insert user (ID=1)
        mockDAO.insert(createValidExistingUser(null, "888"));
        User originalUser = mockDAO.select(null).get(0);

        User userToUpdate = createUserCopy(originalUser);
        userToUpdate.setSurname(""); // Invalid: mandatory field missing

        // Act & Assert
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userService.updateUser(userToUpdate);
        });

        assertTrue(exception.getMessage().contains("Surname is mandatory"));
    }

    @Test
    void testUpdateUser_Failure_DuplicateIDFoundByAnotherUser() throws DAOException {
        // Setup: Two users exist. User1 (ID=1, NID=666) and User2 (ID=2, NID=777)
        mockDAO.insert(createValidExistingUser(null, "666"));
        mockDAO.insert(createValidExistingUser(null, "777"));

        // Get the actual reference to User1 in the database for later assertion
        User originalUser1 = mockDAO.select(null).stream().filter(u -> u.getNationalID().equals("666")).findFirst().get();

        // Create a copy to simulate the update attempt from the UI
        User userAttempt = createUserCopy(originalUser1);

        // Arrange: User1 (ID=1) attempts to change their National ID to 777 (User2's ID)
        userAttempt.setNationalID("777");

        // Act & Assert
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userService.updateUser(userAttempt);
        });

        // Verification: Check the error message
        assertTrue(exception.getMessage().contains("National ID is already registered by another user"));

        // Verification: Ensure the database was not updated by checking the original reference
        // This passes now because userAttempt was modified, not originalUser1.
        assertEquals("666", originalUser1.getNationalID(), "User 1's NID in the database should remain unchanged.");

        // Optional: Re-fetch user 1 from the DB to be absolutely sure
        User userInDb = mockDAO.select(null).stream().filter(u -> u.getIdUser().equals(originalUser1.getIdUser())).findFirst().get();
        assertEquals("666", userInDb.getNationalID(), "User 1's NID re-fetched from database should be unchanged.");
    }

    @Test
    void testUpdateUser_Failure_InvalidNewPassword() throws DAOException {
        // Setup: Insert user (ID=1)
        mockDAO.insert(createValidExistingUser(null, "999"));
        User originalUser = mockDAO.select(null).get(0);

        User userToUpdate = createUserCopy(originalUser);
        userToUpdate.setPassword("short"); // Invalid new password

        // Act & Assert
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userService.updateUser(userToUpdate);
        });

        // Verification
        assertTrue(exception.getMessage().contains("New password must be at least 8 characters"));
        assertEquals(1, mockDAO.select(null).size(), "DB should still have 1 user.");
    }


    // --------------------------------------------------------------------------
    //                             NEW DELETE TESTS
    // --------------------------------------------------------------------------

    @Test
    void testDeleteUser_Success() throws Exception {
        // Setup: Insert user (ID=1)
        mockDAO.insert(createValidExistingUser(null, "D1"));
        User userFromDb = mockDAO.select(null).get(0);

        // Act
        userService.deleteUser(userFromDb);

        // Assert: DB should now be empty
        assertEquals(0, mockDAO.select(null).size(), "User count should be 0 after deletion.");
    }

    @Test
    void testDeleteUser_Failure_MissingID() {
        User user = new User(); // User with no ID set (ID is null)

        // Act & Assert
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userService.deleteUser(user);
        });

        // Verification
        assertTrue(exception.getMessage().contains("Cannot delete user; ID is missing"));
    }

    // --------------------------------------------------------------------------
    //                             DAO EXCEPTION TESTS
    // --------------------------------------------------------------------------

    @Test
    void testUpdateUser_Failure_NonExistentUser() {
        // Arrange: Create a user object that was never inserted (ID is manually set to non-existent value)
        User nonExistent = createValidExistingUser(999, "999");

        // Act & Assert: The MockUserDAOImpl's update method should throw DAOException
        DAOException exception = assertThrows(DAOException.class, () -> {
            userService.updateUser(nonExistent);
        });

        assertTrue(exception.getMessage().contains("User not found for update"));
    }
}