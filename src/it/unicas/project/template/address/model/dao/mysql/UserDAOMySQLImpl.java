package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UserDAOMySQLImpl {

    private static UserDAOMySQLImpl instance = null;
    private static Logger logger = Logger.getLogger(UserDAOMySQLImpl.class.getName());

    private UserDAOMySQLImpl() {}

    public static UserDAOMySQLImpl getInstance() {
        if (instance == null) {
            instance = new UserDAOMySQLImpl();
        }
        return instance;
    }

    // ------------------ METODO NUEVO ------------------
    public User getByUsername(String username) throws DAOException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            logger.info("SQL: " + ps);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("idUser"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("username"),
                        rs.getString("nationalID"),
                        rs.getDate("birthdate") != null ? rs.getDate("birthdate").toLocalDate() : null,
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getInt("idRole")
                );
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error retrieving user by username: " + e.getMessage());
        }
    }


    // ------------------ METODOS EXISTENTES ------------------
    public List<User> select(User u) throws DAOException {
        List<User> list = new ArrayList<>();
        if (u == null) u = new User(null, "", "", "", "", null, "", "", -1);

        String sql = "SELECT * FROM users WHERE 1=1";
        if (u.getIdUser() != -1) sql += " AND idUser=?";
        if (u.getName() != null && !u.getName().isEmpty()) sql += " AND name LIKE ?";
        if (u.getSurname() != null && !u.getSurname().isEmpty()) sql += " AND surname LIKE ?";
        if (u.getUsername() != null && !u.getUsername().isEmpty()) sql += " AND username LIKE ?";
        if (u.getNationalID() != null && !u.getNationalID().isEmpty()) sql += " AND nationalID LIKE ?";
        if (u.getEmail() != null && !u.getEmail().isEmpty()) sql += " AND email LIKE ?";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            int index = 1;
            if (u.getIdUser() != -1) ps.setInt(index++, u.getIdUser());
            if (u.getName() != null && !u.getName().isEmpty()) ps.setString(index++, u.getName() + "%");
            if (u.getSurname() != null && !u.getSurname().isEmpty()) ps.setString(index++, u.getSurname() + "%");
            if (u.getUsername() != null && !u.getUsername().isEmpty()) ps.setString(index++, u.getUsername() + "%");
            if (u.getNationalID() != null && !u.getNationalID().isEmpty()) ps.setString(index++, u.getNationalID() + "%");
            if (u.getEmail() != null && !u.getEmail().isEmpty()) ps.setString(index++, u.getEmail() + "%");

            logger.info("SQL: " + ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getInt("idUser"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("username"),
                        rs.getString("nationalID"),
                        rs.getDate("birthdate") != null ? rs.getDate("birthdate").toLocalDate() : null,
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getInt("idRole")
                );
                list.add(user);
            }
        } catch (SQLException e) {
            throw new DAOException("In select(): " + e.getMessage());
        }
        return list;
    }

    public void insert(User u) throws DAOException {
        if (u == null) throw new DAOException("User cannot be null");
        String sql = "INSERT INTO users (name, surname, username, nationalID, birthdate, password, email, idRole) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getSurname());
            ps.setString(3, u.getUsername());
            ps.setString(4, u.getNationalID());
            if (u.getBirthdate() != null) ps.setDate(5, Date.valueOf(u.getBirthdate()));
            else ps.setNull(5, Types.DATE);
            ps.setString(6, u.getPassword());
            ps.setString(7, u.getEmail());
            ps.setInt(8, u.getIdRole());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) u.setIdUser(rs.getInt(1));

        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    public void update(User u) throws DAOException {
        if (u == null) throw new DAOException("User cannot be null");
        String sql = "UPDATE users SET name=?, surname=?, username=?, nationalID=?, birthdate=?, password=?, email=?, idRole=? WHERE idUser=?";
        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getSurname());
            ps.setString(3, u.getUsername());
            ps.setString(4, u.getNationalID());
            if (u.getBirthdate() != null) ps.setDate(5, Date.valueOf(u.getBirthdate()));
            else ps.setNull(5, Types.DATE);
            ps.setString(6, u.getPassword());
            ps.setString(7, u.getEmail());
            ps.setInt(8, u.getIdRole());
            ps.setInt(9, u.getIdUser());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In update(): " + e.getMessage());
        }
    }

    public void delete(User u) throws DAOException {
        if (u == null || u.getIdUser() == -1) throw new DAOException("idUser cannot be null");
        String sql = "DELETE FROM users WHERE idUser=?";
        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setInt(1, u.getIdUser());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }
}
