package it.unicas.project.template.address.service;
import it.unicas.project.template.address.model.Genre;
import it.unicas.project.template.address.model.dao.GenreDAO;

import java.util.List;

public class GenreService {

    private final GenreDAO dao;

    public GenreService(GenreDAO dao) {
        this.dao = dao;
    }

    public List<Genre> getAllGenres() {
        return dao.selectAll();
    }

    public Integer getGenreId(String genreName) {
        return dao.findIdByName(genreName);
    }
}

