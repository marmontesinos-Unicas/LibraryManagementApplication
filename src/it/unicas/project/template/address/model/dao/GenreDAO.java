package it.unicas.project.template.address.model.dao;

import it.unicas.project.template.address.model.Genre;
import java.util.List;

/**
 * The Data Access Object (DAO) Interface for the Genre entity.
 * <p>
 * This interface defines the contract for accessing and manipulating
 * genre-related data in the persistence layer. Genres are typically
 * static lookup values (e.g., Fiction, Science, History), hence the
 * focus on read and insertion operations.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows the service layer to interact with this DAO interface.
 */
public interface GenreDAO {

    /**
     * Retrieves all {@code Genre} records from the persistence layer.
     * <p>
     * This method is used to populate lists or dropdown menus with all
     * available genre options.
     * </p>
     *
     * @return A list containing all {@code Genre} objects.
     */
    List<Genre> selectAll(); // Read all genres

    /**
     * Finds the unique ID of a genre given its name.
     * <p>
     * This is useful when processing input that provides the genre name
     * (e.g., from a user interface) and the corresponding foreign key ID
     * (idGenre) is required for a database operation.
     * </p>
     *
     * @param name The textual name of the genre (e.g., "Fiction").
     * @return The ID (Integer) of the matching genre, or {@code null} if not found.
     */
    Integer findIdByName(String name); // Get idGenre by text

    /**
     * Inserts a new {@code Genre} record into the persistence layer.
     * <p>
     * This operation allows adding a new genre type to the system.
     * </p>
     *
     * @param g The Genre object to be persisted.
     * @throws DAOException if a database error occurs or the object is invalid (e.g., duplicate name).
     */
    void insert(Genre g) throws DAOException;
}