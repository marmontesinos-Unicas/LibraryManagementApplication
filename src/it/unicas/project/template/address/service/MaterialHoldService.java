package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.model.dao.mysql.HoldDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Service layer that groups material + hold operations into a single transaction.
 * Uses DAO<T> interface types to avoid getInstance() return-type problems.
 */
public class MaterialHoldService {

    // Use the DAO interface type — these getInstance() methods return DAO<T> in your code.
    private final DAO<Material> materialDAO = MaterialDAOMySQLImpl.getInstance();
    private final DAO<Hold> holdDAO = HoldDAOMySQLImpl.getInstance();

    /**
     * Atomically place a hold on the given material for the given user.
     * This method:
     *  - sets material status to "holded" (match your existing status strings)
     *  - inserts a Hold record
     */
    public void holdMaterial(int userId, Material material) throws DAOException {
        Connection conn = null; // should be static in your project
        try {
            conn = DAOMySQLSettings.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            conn.setAutoCommit(false);

            // Update material status
            material.setMaterial_status("holded"); // keep same status string you use elsewhere
            materialDAO.update(material);

            // Create & insert Hold
            Hold hold = new Hold(userId, material.getIdMaterial(), LocalDateTime.now());
            holdDAO.insert(hold);

            conn.commit();
        } catch (Exception e) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            throw new DAOException("Failed to place hold: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
        }
    }

    /**
     * Atomically release a hold (delete hold record and set material to available).
     * Accepts the found Hold instance and the Material to update.
     */
    public void releaseHold(Hold hold, Material material) throws DAOException {
        Connection conn = null;
        try {
            conn = DAOMySQLSettings.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            conn.setAutoCommit(false);

            // delete hold record
            holdDAO.delete(hold);

            // update material status
            material.setMaterial_status("available");
            materialDAO.update(material);

            conn.commit();
        } catch (Exception e) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            throw new DAOException("Failed to release hold: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
        }
    }
}

