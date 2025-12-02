package it.unicas.project.template.address.model.dao;

import it.unicas.project.template.address.model.Genre;
import java.util.List;

public interface GenreDAO {

    List<Genre> selectAll();           // Read all genres
    Integer findIdByName(String name); // Get idGenre by text
    void insert(Genre g) throws DAOException;
}

