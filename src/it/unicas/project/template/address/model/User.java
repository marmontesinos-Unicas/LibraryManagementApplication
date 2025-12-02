package it.unicas.project.template.address.model;

import java.time.LocalDate;
import javafx.beans.property.*;

public class User {

    private IntegerProperty idUser;
    private StringProperty name;
    private StringProperty surname;
    private StringProperty username;
    private StringProperty nationalID;
    private ObjectProperty<LocalDate> birthdate; // DATE → LocalDate
    private StringProperty password;
    private StringProperty email;
    private IntegerProperty idRole; // tabla fija, no hace falta manejo defensivo

    public User() {
        this(null, null, null, null, null, null, null, null, null);
    }

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
        this.idRole = new SimpleIntegerProperty(idRole); // tabla fija
    }

    // GETTERS y SETTERS con Property
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

    public Integer getIdRole() { return idRole.get(); } // tabla fija, sin manejo defensivo
    public void setIdRole(Integer idRole) { this.idRole.set(idRole); }
    public IntegerProperty idRoleProperty() { return idRole; }

    @Override
    public String toString() {
        return name.get() + " " + surname.get() + " (" + getIdUser() + ")";
    }
}
