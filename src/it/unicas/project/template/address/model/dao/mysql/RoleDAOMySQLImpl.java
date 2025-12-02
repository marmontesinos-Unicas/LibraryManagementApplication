package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAOMySQLImpl {

    private static RoleDAOMySQLImpl instance;

    private RoleDAOMySQLImpl() {}

    public static RoleDAOMySQLImpl getInstance() {
        if (instance == null) {
            instance = new RoleDAOMySQLImpl();
        }
        return instance;
    }

    public List<Role> selectAll() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM ROLES";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                roles.add(new Role(
                        rs.getInt("idRole"),
                        rs.getString("admin_type")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error in selectAll(): " + e.getMessage());
        }

        return roles;
    }
}

