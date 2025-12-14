package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.MaterialType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialTypeDAOMySQLImpl {

    private static MaterialTypeDAOMySQLImpl instance;

    private MaterialTypeDAOMySQLImpl() {}

    public static MaterialTypeDAOMySQLImpl getInstance() {
        if (instance == null) {
            instance = new MaterialTypeDAOMySQLImpl();
        }
        return instance;
    }

    public List<MaterialType> selectAll() {
        List<MaterialType> list = new ArrayList<>();
        String sql = "SELECT * FROM MATERIAL_TYPE ORDER BY material_type";

        // FIXED: Added Connection to try-with-resources
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new MaterialType(
                        rs.getInt("idMaterialType"),
                        rs.getString("material_type")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error in selectAll(): " + e.getMessage());
        }

        return list;
    }
}