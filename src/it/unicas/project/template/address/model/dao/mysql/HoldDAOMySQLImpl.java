package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.ExpiringHoldInfo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class HoldDAOMySQLImpl implements DAO<Hold> {

    private static DAO<Hold> dao = null;
    private static Logger logger = null;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected HoldDAOMySQLImpl() {}

    public static DAO<Hold> getInstance() {
        if (dao == null) {
            dao = new HoldDAOMySQLImpl();
            logger = Logger.getLogger(HoldDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    // Assumed Hold Pickup Window: 1 day.
    private static final int HOLD_VALIDITY_DAYS = 1;

    // SQL to fetch ALL holds that are currently valid (Expiration Date > Today)
    private static final String SQL_SELECT_ACTIVE_HOLDS_FOR_USER =
            "SELECT " +
                    "h.idHold, " +
                    "h.hold_date, " +
                    "m.title, " +
                    "m.author " +
                    "FROM " +
                    "holds h " +
                    "JOIN " +
                    "materials m ON h.idMaterial = m.idMaterial " +
                    "WHERE " +
                    "h.idUser = ? " +
                    // Only condition needed: The hold's expiration date (hold_date + X days) is in the future.
                    "AND DATE_ADD(h.hold_date, INTERVAL " + HOLD_VALIDITY_DAYS + " DAY) > CURDATE()";

    @Override
    public List<Hold> select(Hold h) throws DAOException {
        List<Hold> list = new ArrayList<>();
        if (h == null) {
            h = new Hold(); // all -1
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM holds WHERE 1=1");
        if (h.getIdHold() != -1) sql.append(" AND idHold = ?");
        if (h.getIdUser() != -1) sql.append(" AND idUser = ?");
        if (h.getIdMaterial() != -1) sql.append(" AND idMaterial = ?");
        if (h.getHold_date() != null) sql.append(" AND hold_date = ?");

        // FIXED: Added Connection AND ResultSet to try-with-resources
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (h.getIdHold() != -1) ps.setInt(index++, h.getIdHold());
            if (h.getIdUser() != -1) ps.setInt(index++, h.getIdUser());
            if (h.getIdMaterial() != -1) ps.setInt(index++, h.getIdMaterial());
            if (h.getHold_date() != null) ps.setTimestamp(index++, java.sql.Timestamp.valueOf(h.getHold_date()));

            logger.info("SQL: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Hold hold = new Hold(
                            rs.getInt("idHold"),
                            rs.getInt("idUser"),
                            rs.getInt("idMaterial"),
                            rs.getTimestamp("hold_date") != null ? rs.getTimestamp("hold_date").toLocalDateTime() : null
                    );
                    list.add(hold);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("In select(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all holds for a user that are about to expire (within 2 days).
     * @param userId The ID of the currently logged-in user.
     * @return A list of ExpiringHoldInfo objects.
     * @throws DAOException if a database error occurs.
     */
    public List<ExpiringHoldInfo> getExpiringHoldsForUser(int userId) throws DAOException {
        List<ExpiringHoldInfo> expiringHolds = new ArrayList<>();

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ACTIVE_HOLDS_FOR_USER)) {

            ps.setInt(1, userId);

            logger.info("SQL: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int holdId = rs.getInt("idHold");
                    String title = rs.getString("title");
                    String author = rs.getString("author");

                    // The 'hold_date' from DB is the creation date. We calculate the expiration date.
                    // Convert java.sql.Timestamp to java.time.LocalDateTime for the creation date
                    LocalDateTime holdDateCreation = rs.getTimestamp("hold_date").toLocalDateTime();

                    // Calculate the final Expiration Date (Creation Date + HOLD_VALIDITY_DAYS)
                    LocalDate holdExpirationDate = holdDateCreation.toLocalDate().plusDays(HOLD_VALIDITY_DAYS);

                    ExpiringHoldInfo item = new ExpiringHoldInfo(holdId, title, author, holdExpirationDate);
                    expiringHolds.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("In getExpiringHoldsForUser(): " + e.getMessage());
        }

        return expiringHolds;
    }

    @Override
    public void insert(Hold h) throws DAOException {
        verifyObject(h);
        String sql = "INSERT INTO holds (idUser, idMaterial, hold_date) VALUES (?, ?, ?)";

        // FIXED: Added Connection to try-with-resources
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, h.getIdUser());
            ps.setInt(2, h.getIdMaterial());
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(h.getHold_date()));
            logger.info("SQL: " + ps);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) h.setIdHold(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    @Override
    public void update(Hold h) throws DAOException {
        verifyObject(h);
        String sql = "UPDATE holds SET idUser=?, idMaterial=?, hold_date=? WHERE idHold=?";

        // FIXED: Added Connection to try-with-resources
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, h.getIdUser());
            ps.setInt(2, h.getIdMaterial());
            ps.setString(3, h.getHold_date().format(FORMATTER));
            ps.setInt(4, h.getIdHold());

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

        // FIXED: Added Connection to try-with-resources
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, h.getIdHold());
            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }

    @Override
    public List<Hold> selectAll() throws DAOException {
        return select(null);
    }

    private void verifyObject(Hold h) throws DAOException {
        if (h == null || h.getIdUser() == -1 ||
                h.getIdMaterial() == -1 || h.getHold_date() == null) {
            throw new DAOException("In verifyObject: all fields must be non-null or valid");
        }
    }
}