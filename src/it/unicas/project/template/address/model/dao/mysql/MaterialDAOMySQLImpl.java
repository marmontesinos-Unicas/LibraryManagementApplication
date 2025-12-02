package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.MaterialGenre;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MaterialDAOMySQLImpl implements DAO<Material> {

    private static DAO<Material> dao = null;
    private static Logger logger = null;

    private MaterialDAOMySQLImpl() {}

    public static DAO<Material> getInstance() {
        if (dao == null) {
            dao = new MaterialDAOMySQLImpl();
            logger = Logger.getLogger(MaterialDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    @Override
    public List<Material> select(Material m) throws DAOException {
        List<Material> list = new ArrayList<>();
        if (m == null) m = new Material("", "", null, "", null, "");

        String sql = "SELECT * FROM materials WHERE 1=1";
        if (m.getIdMaterial() != -1) sql += " AND idMaterial=?";
        if (m.getTitle() != null && !m.getTitle().isEmpty()) sql += " AND title LIKE ?";
        if (m.getAuthor() != null && !m.getAuthor().isEmpty()) sql += " AND author LIKE ?";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            int index = 1;
            if (m.getIdMaterial() != -1) ps.setInt(index++, m.getIdMaterial());
            if (m.getTitle() != null && !m.getTitle().isEmpty()) ps.setString(index++, m.getTitle() + "%");
            if (m.getAuthor() != null && !m.getAuthor().isEmpty()) ps.setString(index++, m.getAuthor() + "%");

            logger.info("SQL: " + ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Material mat = new Material(
                        rs.getInt("idMaterial"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("year"),
                        rs.getString("ISBN"),
                        rs.getInt("idMaterialType"),
                        rs.getString("material_status")
                );
                list.add(mat);
            }
        } catch (SQLException e) {
            throw new DAOException("In select(): " + e.getMessage());
        }
        return list;
    }

    @Override
    public void insert(MaterialGenre mg) throws DAOException {

    }

    @Override
    public void insert(Material m) throws DAOException {
        verifyObject(m);
        String sql = "INSERT INTO materials (title, author, year, ISBN, idMaterialType, material_status) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getTitle());
            ps.setString(2, m.getAuthor());
            ps.setInt(3, m.getYear());
            ps.setString(4, m.getISBN());
            ps.setInt(5, m.getIdMaterialType());
            ps.setString(6, m.getMaterial_status());

            logger.info("SQL: " + ps);
            ps.executeUpdate();

            // Obtener idMaterial generado automáticamente
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) m.setIdMaterial(rs.getInt(1));

        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    @Override
    public void update(Material m) throws DAOException {
        verifyObject(m);
        String sql = "UPDATE materials SET title=?, author=?, year=?, ISBN=?, idMaterialType=?, material_status=? WHERE idMaterial=?";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getAuthor());
            ps.setInt(3, m.getYear());
            ps.setString(4, m.getISBN());
            ps.setInt(5, m.getIdMaterialType());
            ps.setString(6, m.getMaterial_status());
            ps.setInt(7, m.getIdMaterial());

            logger.info("SQL: " + ps);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("In update(): " + e.getMessage());
        }
    }

    @Override
    public void delete(Material m) throws DAOException {
        if (m == null || m.getIdMaterial() == -1) {
            throw new DAOException("In delete: idMaterial cannot be null");
        }

        String sql = "DELETE FROM materials WHERE idMaterial=?";
        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setInt(1, m.getIdMaterial());
            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }

    private void verifyObject(Material m) throws DAOException {
        if (m == null || m.getTitle() == null || m.getIdMaterialType() == null
                || m.getMaterial_status() == null) {
            throw new DAOException("In verifyObject: all fields must be non-null");
        }
    }
}
