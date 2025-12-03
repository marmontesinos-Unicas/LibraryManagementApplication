package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.MaterialGenre;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MaterialGenreDAOMySQLImpl implements DAO<MaterialGenre> {

    private static DAO<MaterialGenre> dao = null;
    private static Logger logger = null;

    private MaterialGenreDAOMySQLImpl() {}

    public static DAO<MaterialGenre> getInstance() {
        if (dao == null) {
            dao = new MaterialGenreDAOMySQLImpl();
            logger = Logger.getLogger(MaterialGenreDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    @Override
    public List<MaterialGenre> select(MaterialGenre mg) throws DAOException {
        List<MaterialGenre> list = new ArrayList<>();
        String sql = "SELECT * FROM materials_genres WHERE 1=1";

        if (mg != null) {
            if (mg.getIdMaterial() != -1) sql += " AND idMaterial=?";
            if (mg.getIdGenre() != -1) sql += " AND idGenre=?";
        }

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            int index = 1;
            if (mg != null) {
                if (mg.getIdMaterial() != -1) ps.setInt(index++, mg.getIdMaterial());
                if (mg.getIdGenre() != -1) ps.setInt(index++, mg.getIdGenre());
            }

            logger.info("SQL: " + ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new MaterialGenre(
                        rs.getInt("idMaterial"),
                        rs.getInt("idGenre")
                ));
            }
        } catch (SQLException e) {
            throw new DAOException("In select(): " + e.getMessage());
        }

        return list;
    }

    @Override
    public void insert(MaterialGenre mg) throws DAOException {
        verifyObject(mg);
        String sql = "INSERT INTO materials_genres (idMaterial, idGenre) VALUES (?, ?)";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setInt(1, mg.getIdMaterial());
            ps.setInt(2, mg.getIdGenre());

            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    @Override
    public void update(MaterialGenre mg) throws DAOException {
        // No se puede hacer update porque la PK es compuesta
        throw new DAOException("Update no soportado para MaterialGenre (PK compuesta)");
    }

    @Override
    public void delete(MaterialGenre mg) throws DAOException {
        if (mg == null || mg.getIdMaterial() == -1 || mg.getIdGenre() == -1) {
            throw new DAOException("In delete: idMaterial y idGenre no pueden ser nulos");
        }

        String sql = "DELETE FROM materials_genres WHERE idMaterial=? AND idGenre=?";
        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setInt(1, mg.getIdMaterial());
            ps.setInt(2, mg.getIdGenre());

            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }

    private void verifyObject(MaterialGenre mg) throws DAOException {
        if (mg == null || mg.getIdMaterial() == -1 || mg.getIdGenre() == -1) {
            throw new DAOException("In verifyObject: idMaterial y idGenre deben ser válidos");
        }
    }
}