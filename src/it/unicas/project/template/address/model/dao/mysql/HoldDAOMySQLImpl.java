package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class HoldDAOMySQLImpl implements DAO<Hold> {

    private static DAO<Hold> dao = null;
    private static Logger logger = null;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private HoldDAOMySQLImpl() {}

    public static DAO<Hold> getInstance() {
        if (dao == null) {
            dao = new HoldDAOMySQLImpl();
            logger = Logger.getLogger(HoldDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    @Override
    public List<Hold> select(Hold h) throws DAOException {
        List<Hold> list = new ArrayList<>();
        if (h == null) h = new Hold(null, null, null, null, null);

        String sql = "SELECT * FROM holds WHERE 1=1";
        if (h.getIdHold() != -1) sql += " AND idHold=?";
        if (h.getIdUser() != -1) sql += " AND idUser=?";
        if (h.getIdMaterial() != -1) sql += " AND idMaterial=?";
        if (h.getHold_date() != null) sql += " AND hold_date LIKE ?";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            int index = 1;
            if (h.getIdHold() != -1) ps.setInt(index++, h.getIdHold());
            if (h.getIdUser() != -1) ps.setInt(index++, h.getIdUser());
            if (h.getIdMaterial() != -1) ps.setInt(index++, h.getIdMaterial());
            if (h.getHold_date() != null) ps.setString(index++, h.getHold_date().format(FORMATTER));

            logger.info("SQL: " + ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Hold hold = new Hold(
                        rs.getInt("idHold"),
                        rs.getInt("idUser"),
                        rs.getInt("idMaterial"),
                        rs.getTimestamp("hold_date").toLocalDateTime(),
                        rs.getString("hold_status")
                );
                list.add(hold);
            }
        } catch (SQLException e) {
            throw new DAOException("In select(): " + e.getMessage());
        }

        return list;
    }

    @Override
    public void insert(Hold h) throws DAOException {
        verifyObject(h);
        String sql = "INSERT INTO holds (idUser, idMaterial, hold_date, hold_status) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getIdUser());
            ps.setInt(2, h.getIdMaterial());
            ps.setString(3, h.getHold_date().format(FORMATTER));
            ps.setString(4, h.getHold_status());

            logger.info("SQL: " + ps);
            ps.executeUpdate();

            // Obtener idHold generado automáticamente
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) h.setIdHold(rs.getInt(1));
        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    @Override
    public void update(Hold h) throws DAOException {
        verifyObject(h);
        String sql = "UPDATE holds SET idUser=?, idMaterial=?, hold_date=?, hold_status=? WHERE idHold=?";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setInt(1, h.getIdUser());
            ps.setInt(2, h.getIdMaterial());
            ps.setString(3, h.getHold_date().format(FORMATTER));
            ps.setString(4, h.getHold_status());
            ps.setInt(5, h.getIdHold());

            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In update(): " + e.getMessage());
        }
    }

    @Override
    public void delete(Hold h) throws DAOException {
        if (h == null || h.getIdHold() == -1) {
            throw new DAOException("In delete: idHold cannot be null");
        }

        String sql = "DELETE FROM holds WHERE idHold=?";
        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setInt(1, h.getIdHold());
            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }

    private void verifyObject(Hold h) throws DAOException {
        if (h == null || h.getIdUser() == -1 || h.getIdMaterial() == -1 ||
                h.getHold_date() == null || h.getHold_status() == null) {
            throw new DAOException("In verifyObject: all fields must be non-null or valid");
        }
    }
}
