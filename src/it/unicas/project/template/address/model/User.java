package it.unicas.project.template.address.model;

import java.time.LocalDate;
import javafx.beans.property.*;

/**
 * Represents a user in the library system for the JavaFX UI.
 * <p>
 * Provides JavaFX properties for TableView and other UI bindings.
 * Can be persisted using the DAO layer.
 * </p>
 */
public class User {

    /** Unique identifier of the user (Primary Key) */
    private IntegerProperty idUser;

    /** First name of the user */
    private StringProperty name;

    /** Last name of the user */
    private StringProperty surname;

    /** Username used for login */
    private StringProperty username;

    /** National ID (DNI, passport, etc.) */
    private StringProperty nationalID;

    /** Date of birth */
    private ObjectProperty<LocalDate> birthdate;

    /** Password (hashed or plain depending on implementation) */
    private StringProperty password;

    /** Email address */
    private StringProperty email;

    /** Role ID (foreign key to Role table, fixed table) */
    private IntegerProperty idRole;

    /**
     * Default constructor.
     * Initializes all fields to default values.
     */
    public User() {
        this(null, null, null, null, null, null, null, null, null);
    }

    /**
     * Constructs a User with all fields specified.
     *
     * @param idUser unique user ID
     * @param name first name
     * @param surname last name
     * @param username login username
     * @param nationalID national identification
     * @param birthdate date of birth
     * @param password user password
     * @param email email address
     * @param idRole role ID
     */
    public User(Integer idUser, String name, String surname, String username,
                String nationalID, LocalDate birthdate, String password,
                String email, Integer idRole) {
        this.idUser = idUser != null ? new SimpleIntegerProperty(idUser) : null;
        this.name = new SimpleStringProperty(name);
        this.surname = new SimpleStringProperty(surname);
        this.username = new SimpleStringProperty(username);
        this.nationalID = new SimpleStringProperty(nationalID);
        this.birthdate = new SimpleObjectProperty<>(birthdate);
        this.password = new SimpleStringProperty(password);
        this.email = new SimpleStringProperty(email);
        this.idRole = idRole != null ? new SimpleIntegerProperty(idRole) : null;
    }

    /** Getters, setters, and JavaFX properties for UI binding */
    public Integer getIdUser() {
        if (idUser == null) idUser = new SimpleIntegerProperty(-1);
        return idUser.get();
    }
    public void setIdUser(Integer idUser) {
        if (this.idUser == null) this.idUser = new SimpleIntegerProperty();
        this.idUser.set(idUser);
    }
    public IntegerProperty idUserProperty() {
        if (idUser == null) idUser = new SimpleIntegerProperty();
        return idUser;
    }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getSurname() { return surname.get(); }
    public void setSurname(String surname) { this.surname.set(surname); }
    public StringProperty surnameProperty() { return surname; }

    public String getUsername() { return username.get(); }
    public void setUsername(String username) { this.username.set(username); }
    public StringProperty usernameProperty() { return username; }

    public String getNationalID() { return nationalID.get(); }
    public void setNationalID(String nationalID) { this.nationalID.set(nationalID); }
    public StringProperty nationalIDProperty() { return nationalID; }

    public LocalDate getBirthdate() { return birthdate.get(); }
    public void setBirthdate(LocalDate birthdate) { this.birthdate.set(birthdate); }
    public ObjectProperty<LocalDate> birthdateProperty() { return birthdate; }

    public String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }
    public StringProperty passwordProperty() { return password; }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    public Integer getIdRole() { return idRole.get(); }
    public void setIdRole(Integer idRole) { this.idRole.set(idRole); }
    public IntegerProperty idRoleProperty() { return idRole; }

    /**
     * Returns a string representation of the user.
     *
     * @return user's full name and ID
     */
    @Override
    public String toString() {
        return name.get() + " " + surname.get() + " (" + getIdUser() + ")";
    }
}
