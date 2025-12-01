package it.unicas.project.template.address.model;

public class User {

    private Integer idUser;
    private String name;
    private String surname;
    private String username;
    private String nationalID;
    private String password;
    private String email;
    private Integer idRole;

    public User() {}

    public User(Integer idUser, String name, String surname, String username,
                String nationalID, String password, String email, Integer idRole) {
        this.idUser = idUser;
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.nationalID = nationalID;
        this.password = password;
        this.email = email;
        this.idRole = idRole;
    }

    public User(String name, String surname, String username,
                String nationalID, String password, String email, Integer idRole) {
        this(null, name, surname, username, nationalID, password, email, idRole);
    }

    // GETTERS AND SETTERS
    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNationalID() { return nationalID; }
    public void setNationalID(String nationalID) { this.nationalID = nationalID; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getIdRole() { return idRole; }
    public void setIdRole(Integer idRole) { this.idRole = idRole; }

    @Override
    public String toString() {
        return name + " " + surname + " (" + idUser + ")";
    }
}
