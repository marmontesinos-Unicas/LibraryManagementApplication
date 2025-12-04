package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Material;
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

        String sql = "SELECT m.*, mt.material_type " +
                "FROM materials m " +
                "LEFT JOIN material_type mt ON m.idMaterialType = mt.idMaterialType " +
                "WHERE 1=1"; // to join with material type for displaying the name

        if (m.getIdMaterial() != -1)
            sql += " AND m.idMaterial=?";
        if (m.getTitle() != null && !m.getTitle().isEmpty())
            sql += " AND m.title LIKE ?";
        if (m.getAuthor() != null && !m.getAuthor().isEmpty())
            sql += " AND m.author LIKE ?";
        if (m.getISBN() != null && !m.getISBN().isEmpty())
            sql += " AND m.ISBN LIKE ?";
        if (m.getIdMaterialType() != null && m.getIdMaterialType() != 0)
            sql += " AND m.idMaterialType=?";
        if (m.getMaterial_status() != null && !m.getMaterial_status().isEmpty())
            sql += " AND m.material_status=?";

        sql += " ORDER BY m.title";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            int index = 1;
            if (m.getIdMaterial() != -1)
                ps.setInt(index++, m.getIdMaterial());
            if (m.getTitle() != null && !m.getTitle().isEmpty())
                ps.setString(index++, "%" + m.getTitle() + "%");
            if (m.getAuthor() != null && !m.getAuthor().isEmpty())
                ps.setString(index++, "%" + m.getAuthor() + "%");
            if (m.getISBN() != null && !m.getISBN().isEmpty())
                ps.setString(index++, "%" + m.getISBN() + "%");
            if (m.getIdMaterialType() != null && m.getIdMaterialType() != 0)
                ps.setInt(index++, m.getIdMaterialType());
            if (m.getMaterial_status() != null && !m.getMaterial_status().isEmpty())
                ps.setString(index++, m.getMaterial_status());


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

    /**
     * Convenience method to find all materials
     * Returns all materials ordered by title
     */
    public List<Material> selectAll() throws DAOException {
        return select(null);
    }


    /**
     * Find materials by a search term that matches title, author, or ISBN
     * Useful for a single search box in the UI
     */
    public List<Material> selectBySearchTerm(String searchTerm) throws DAOException {
        List<Material> list = new ArrayList<>();

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return selectAll();
        }

        String sql = "SELECT m.*, mt.material_type " +
                "FROM materials m " +
                "LEFT JOIN material_type mt ON m.idMaterialType = mt.idMaterialType " +
                "WHERE m.title LIKE ? OR m.author LIKE ? OR m.ISBN LIKE ? " +
                "ORDER BY m.title ASC";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

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
            throw new DAOException("In selectBySearchTerm(): " + e.getMessage());
        }

        return list;
    }

    /**
     * Find materials by availability status
     */
    public List<Material> selectByStatus(String status) throws DAOException {
        Material criteria = new Material();
        criteria.setMaterial_status(status);
        return select(criteria);
    }

    /**
     * Find materials by type
     */
    public List<Material> selectByType(Integer idMaterialType) throws DAOException {
        Material criteria = new Material();
        criteria.setIdMaterialType(idMaterialType);
        return select(criteria);
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
            if (rs.next())
                m.setIdMaterial(rs.getInt(1));

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
        if (m == null || m.getTitle() == null || m.getTitle().isEmpty()
                || m.getIdMaterialType() == null
                || m.getMaterial_status() == null || m.getMaterial_status().isEmpty())  {
            throw new DAOException("In verifyObject: all fields must be non-null");
        }
    }
}
