package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Genre;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.GenreDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * MySQL implementation of the GenreDAO interface.
 * ALL CONNECTION LEAKS FIXED - Ready to use
 */
public class GenreDAOMySQLImpl implements GenreDAO {

    private static GenreDAOMySQLImpl instance;
    private static Logger logger = null;

    private GenreDAOMySQLImpl() {}

    public static GenreDAOMySQLImpl getInstance() {
        if (instance == null) {
            instance = new GenreDAOMySQLImpl();
            logger = Logger.getLogger(GenreDAOMySQLImpl.class.getName());
        }
        return instance;
    }

    @Override
    public List<Genre> selectAll() {
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT * FROM GENRE ORDER BY genre";

        // FIXED: Added Connection to try-with-resources
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                genres.add(new Genre(
                        rs.getInt("idGenre"),
                        rs.getString("genre")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error in selectAll(): " + e.getMessage());
            logger.severe("Error in selectAll(): " + e.getMessage());
        }

        return genres;
    }

    @Override
    public Integer findIdByName(String name) {
        String sql = "SELECT idGenre FROM GENRE WHERE genre=?";

        // FIXED: Added Connection AND ResultSet to try-with-resources
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idGenre");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error in findIdByName(): " + e.getMessage());
            logger.severe("Error in findIdByName(): " + e.getMessage());
        }
        return null;
    }

    @Override
    public void insert(Genre g) throws DAOException {
        String sql = "INSERT INTO GENRE (genre) VALUES (?)";

        // FIXED: Added Connection to try-with-resources
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, g.getGenre());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }
}
