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
 * Corrected Mock implementation of the UserDAOMySQLImpl.
 * It now filters by both NationalID AND Role to align with UserService business logic.
 */
class MockUserDAOImpl extends UserDAOMySQLImpl {
    private final List<User> database = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public void reset() {
        database.clear();
        nextId.set(1);
    }

    @Override
    public List<User> select(User u) throws DAOException {
        // If the filter is null, return all users
        if (u == null) return new ArrayList<>(database);

        // Service uniqueness check uses NationalID + Role
        return database.stream()
                .filter(user -> {
                    boolean nidMatch = (u.getNationalID() == null || u.getNationalID().isEmpty()) ||
                            user.getNationalID().equals(u.getNationalID());

                    // Match Role if provided (Service sets this to check uniqueness)
                    boolean roleMatch = (u.getIdRole() == null || u.getIdRole() == -1) ||
                            user.getIdRole().equals(u.getIdRole());

                    return nidMatch && roleMatch;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void insert(User u) throws DAOException {
        User newUser = new User(
                nextId.getAndIncrement(),
                u.getName(), u.getSurname(), u.getUsername(), u.getNationalID(),
                u.getBirthdate(), u.getPassword(), u.getEmail(), u.getIdRole()
        );
        database.add(newUser);
    }

    @Override
    public void update(User u) throws DAOException {
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
        boolean removed = database.removeIf(user -> user.getIdUser().equals(u.getIdUser()));
        if (!removed) {
            throw new DAOException("User not found for deletion: ID " + u.getIdUser());
        }
    }
}

public class UserServiceTest {

    private UserService userService;
    private MockUserDAOImpl mockDAO;
    private final LocalDate birthday = LocalDate.of(2002, 10, 20);

    @BeforeEach
    public void setUp() {
        mockDAO = new MockUserDAOImpl();
        mockDAO.reset();
        userService = new UserService(mockDAO); // Inject corrected mock
    }

    private User createValidUser(String nationalID, Integer role) {
        return new User(null, "TestName", "TestSurname", "TestUser", nationalID,
                birthday, "StrongP@ss1", "test@example.com", role);
    }

    private User createUserCopy(User original) {
        return new User(original.getIdUser(), original.getName(), original.getSurname(),
                original.getUsername(), original.getNationalID(), original.getBirthdate(),
                original.getPassword(), original.getEmail(), original.getIdRole());
    }

    // --- REGISTRATION TESTS ---

    @Test
    public void testRegisterUserSuccess() throws Exception {
        User newUser = createValidUser("12345678X", 2);
        userService.registerUser(newUser);

        assertEquals(1, mockDAO.select(null).size());
    }

    @Test
    public void testRegisterUserDuplicateNationalIdThrowsServiceException() throws DAOException {
        // Setup: Existing user with NID "NID123" and Role 2
        mockDAO.insert(createValidUser("NID123", 2));

        // Attempt: Register another user with SAME NID and SAME Role
        User newUser = createValidUser("NID123", 2);

        Exception ex = assertThrows(UserServiceException.class, () -> userService.registerUser(newUser));
        assertTrue(ex.getMessage().contains("National ID and Role is already registered"));
    }

    // --- UPDATE TESTS ---

    @Test
    void testUpdateUser_Success() throws Exception {
        mockDAO.insert(createValidUser("444", 2));
        User original = mockDAO.select(null).get(0);

        User toUpdate = createUserCopy(original);
        toUpdate.setSurname("UpdatedName");
        toUpdate.setPassword(null); // UserService allows null password on update

        userService.updateUser(toUpdate);
        assertEquals("UpdatedName", mockDAO.select(null).get(0).getSurname());
    }

    @Test
    void testUpdateUser_Failure_DuplicateIDFoundByAnotherUser() throws DAOException {
        // Setup: Two users in the same role
        mockDAO.insert(createValidUser("666", 2));
        mockDAO.insert(createValidUser("777", 2));

        User user1 = mockDAO.select(null).stream().filter(u -> u.getNationalID().equals("666")).findFirst().get();
        User updateAttempt = createUserCopy(user1);

        // Attempt to take User 2's National ID
        updateAttempt.setNationalID("777");

        UserServiceException ex = assertThrows(UserServiceException.class, () -> userService.updateUser(updateAttempt));
        assertTrue(ex.getMessage().contains("combination is already used by another user"));
    }

    // --- DELETE TESTS ---

    @Test
    void testDeleteUser_Success() throws Exception {
        mockDAO.insert(createValidUser("D1", 2));
        User user = mockDAO.select(null).get(0);

        userService.deleteUser(user);
        assertEquals(0, mockDAO.select(null).size());
    }
}