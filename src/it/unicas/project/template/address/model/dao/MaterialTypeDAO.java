package it.unicas.project.template.address.model.dao;

import it.unicas.project.template.address.model.MaterialType;
import java.util.List;

public interface MaterialTypeDAO {
    List<MaterialType> selectAll() throws DAOException;
}

