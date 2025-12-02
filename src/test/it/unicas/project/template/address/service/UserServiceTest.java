package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    // Fake DAO to avoid DB access and to capture calls
    static class FakeUserDAO extends UserDAOMySQLImpl {
        private List<User> selectResult = new ArrayList<>();
        private boolean insertCalled = false;
        private User lastInserted = null;

        public FakeUserDAO() {
            super();
            // may call the parent constructor; adjust if parent requires params
        }

        public void setSelectResult(List<User> res) {
            this.selectResult = res;
        }

        public boolean wasInsertCalled() {
            return insertCalled;
        }

        public User getLastInserted() {
            return lastInserted;
        }

        @Override
        public List<User> select(User u) throws DAOException {
            return selectResult;
        }

        @Override
        public void insert(User u) throws DAOException {
            insertCalled = true;
            lastInserted = u;
        }
    }

    private FakeUserDAO fakeDao;
    private UserService userService;
    LocalDate birthday = LocalDate.of(2002, 10, 20);

    @BeforeEach
    public void setUp() {
        fakeDao = new FakeUserDAO();
        userService = new UserService(fakeDao); // requires UserService(UserDAOMySQLImpl) constructor
    }

    @Test
    public void testRegisterUserSuccess() throws Exception {
        // Arrange: no existing users
        fakeDao.setSelectResult(new ArrayList<>());

        // Use a password that satisfies the policy: >=8 chars, 1 uppercase, 1 digit
        User newUser = new User(null, "John", "Doe", "jdoe", "NID123", birthday, "Password1", "jdoe@example.com", 1);

        // Act
        userService.registerUser(newUser);

        // Assert
        assertTrue(fakeDao.wasInsertCalled(), "Expected insert to be called on successful registration");
        assertNotNull(fakeDao.getLastInserted(), "Inserted user should be captured");
        assertEquals("NID123", fakeDao.getLastInserted().getNationalID());
        assertEquals("John", fakeDao.getLastInserted().getName());
    }

    @Test
    public void testRegisterUserMissingNameThrowsServiceException() {
        // Arrange: name is missing; password still valid to avoid masking other validations
        User newUser = new User(null, "", "Doe", "jdoe", "NID123", birthday, "Password1", "jdoe@example.com", 1);

        // Act & Assert
        Exception ex = assertThrows(UserServiceException.class, () -> userService.registerUser(newUser));
        String msg = ex.getMessage();
        assertTrue(msg != null && (msg.toLowerCase().contains("name") || msg.toLowerCase().contains("mandatory")));
    }

    @Test
    public void testRegisterUserDuplicateNationalIdThrowsServiceException() {
        // Arrange: select returns a non-empty list -> duplicate national ID
        List<User> existing = new ArrayList<>();
        existing.add(new User(1, "Existing", "User", "exist", "NID123", birthday, "Password1", "e@e.com", 1));
        fakeDao.setSelectResult(existing);

        User newUser = new User(null, "John", "Doe", "jdoe", "NID123", birthday, "Password1", "jdoe@example.com", 1);

        // Act & Assert
        Exception ex = assertThrows(UserServiceException.class, () -> userService.registerUser(newUser));
        String msg = ex.getMessage();
        assertTrue(msg != null && (msg.toLowerCase().contains("already") || msg.toLowerCase().contains("registered")));
    }

    @Test
    public void testRegisterUserInvalidPasswordThrowsServiceException() {
        // Arrange: ensure no existing users (so password validation is reached)
        fakeDao.setSelectResult(new ArrayList<>());

        // invalid password: all lowercase, no digit, length >= 8 but misses uppercase/digit
        User newUser = new User(null, "Alice", "Smith", "asmith", "NID999", birthday, "password", "alice@example.com", 1);

        // Act & Assert: password policy should trigger UserServiceException
        Exception ex = assertThrows(UserServiceException.class, () -> userService.registerUser(newUser));
        String msg = ex.getMessage();
        assertTrue(msg != null && (msg.toLowerCase().contains("password") || msg.toLowerCase().contains("at least")));
    }
}
