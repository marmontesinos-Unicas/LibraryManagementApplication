package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LoanDAOMySQLImpl implements DAO<Loan> {

    private static DAO<Loan> dao = null;
    private static Logger logger = null;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LoanDAOMySQLImpl() {}

    public static DAO<Loan> getInstance() {
        if (dao == null) {
            dao = new LoanDAOMySQLImpl();
            logger = Logger.getLogger(LoanDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    @Override
    public List<Loan> select(Loan l) throws DAOException {
        List<Loan> list = new ArrayList<>();
        if (l == null) l = new Loan(null, null, null, null, null);

        String sql = "SELECT * FROM loans WHERE 1=1";
        if (l.getIdLoan() != -1) sql += " AND idLoan=?";
        if (l.getIdUser() != -1) sql += " AND idUser=?";
        if (l.getIdMaterial() != -1) sql += " AND idMaterial=?";
        if (l.getStart_date() != null) sql += " AND start_date LIKE ?";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            int index = 1;
            if (l.getIdLoan() != -1) ps.setInt(index++, l.getIdLoan());
            if (l.getIdUser() != -1) ps.setInt(index++, l.getIdUser());
            if (l.getIdMaterial() != -1) ps.setInt(index++, l.getIdMaterial());
            if (l.getStart_date() != null) ps.setString(index++, l.getStart_date().format(FORMATTER) + "%");

            logger.info("SQL: " + ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Loan loan = new Loan(
                        rs.getInt("idLoan"),
                        rs.getInt("idUser"),
                        rs.getInt("idMaterial"),
                        rs.getTimestamp("start_date") != null ? rs.getTimestamp("start_date").toLocalDateTime() : null,
                        rs.getTimestamp("due_date") != null ? rs.getTimestamp("due_date").toLocalDateTime() : null,
                        rs.getTimestamp("return_date") != null ? rs.getTimestamp("return_date").toLocalDateTime() : null
                );
                list.add(loan);
            }
        } catch (SQLException e) {
            throw new DAOException("In select(): " + e.getMessage());
        }

        return list;
    }

    @Override
    public void insert(Loan l) throws DAOException {
        verifyObject(l);
        String sql = "INSERT INTO loans (idUser, idMaterial, start_date, due_date, return_date) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, l.getIdUser());
            ps.setInt(2, l.getIdMaterial());
            ps.setString(3, l.getStart_date().format(FORMATTER));
            ps.setString(4, l.getDue_date().format(FORMATTER));
            if (l.getReturn_date() != null) {
                ps.setString(5, l.getReturn_date().format(FORMATTER));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            logger.info("SQL: " + ps);
            ps.executeUpdate();

            // Obtener idLoan generado automáticamente
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) l.setIdLoan(rs.getInt(1));
        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    @Override
    public void update(Loan l) throws DAOException {
        verifyObject(l);
        String sql = "UPDATE loans SET idUser=?, idMaterial=?, start_date=?, due_date=?, return_date=? WHERE idLoan=?";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setInt(1, l.getIdUser());
            ps.setInt(2, l.getIdMaterial());
            ps.setString(3, l.getStart_date().format(FORMATTER));
            ps.setString(4, l.getDue_date().format(FORMATTER));
            if (l.getReturn_date() != null) {
                ps.setString(5, l.getReturn_date().format(FORMATTER));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.setInt(6, l.getIdLoan());

            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In update(): " + e.getMessage());
        }
    }

    @Override
    public void delete(Loan l) throws DAOException {
        if (l == null || l.getIdLoan() == -1) {
            throw new DAOException("In delete: idLoan cannot be null");
        }

        String sql = "DELETE FROM loans WHERE idLoan=?";
        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setInt(1, l.getIdLoan());
            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }

    private void verifyObject(Loan l) throws DAOException {
        if (l == null || l.getIdUser() == -1 || l.getIdMaterial() == -1 ||
                l.getStart_date() == null || l.getDue_date() == null) {
            throw new DAOException("In verifyObject: required fields must be non-null or valid");
        }
    }

    // *** NEW METHOD ADDED FOR DELETION VALIDATION ***
    /**
     * Counts the number of loans associated with a user that have not yet been returned.
     * An active loan is defined as one where the return_date is NULL.
     * @param userId The ID of the user.
     * @return The count of active loans.
     * @throws DAOException if a database error occurs.
     */
    public int countActiveLoansByUserId(int userId) throws DAOException {
        String sql = "SELECT COUNT(*) FROM loans WHERE idUser = ? AND return_date IS NULL";
        int count = 0;
        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);

            logger.info("SQL: " + ps);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException("In countActiveLoansByUserId(): " + e.getMessage());
        }
        return count;
    }
}