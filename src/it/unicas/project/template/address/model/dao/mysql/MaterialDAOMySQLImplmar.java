package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Materialmar;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MaterialDAOMySQLImplmar implements DAO<Materialmar> {

    private MaterialDAOMySQLImplmar(){}

    private static DAO dao = null;
    private static Logger logger = null;

    public static DAO getInstance(){
        if(dao == null){
            dao = new MaterialDAOMySQLImplmar();
            logger = Logger.getLogger(MaterialDAOMySQLImplmar.class.getName());
        }
        return dao;
    }

    @Override
    public List<Materialmar> select(Materialmar m) throws DAOException {
        if(m == null){
            m = new Materialmar("", "", null, "", null, null, "");
        }
        ArrayList<Materialmar> list = new ArrayList<>();
        try{
            Statement st = DAOMySQLSettings.getStatement();
            String sql = "SELECT * FROM materials WHERE title LIKE '" + m.getTitle() + "%'" +
                    " AND author LIKE '" + m.getAuthor() + "%'";
            logger.info("SQL: " + sql);
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()){
                list.add(new Materialmar(
                        rs.getInt("idMaterial"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("year"),
                        rs.getString("ISBN"),
                        rs.getInt("idMaterialType"),
                        rs.getString("material_status")
                ));
            }
            DAOMySQLSettings.closeStatement(st);
        } catch(SQLException e){
            throw new DAOException("In select(): " + e.getMessage());
        }
        return list;
    }

    @Override
    public void insert(Materialmar m) throws DAOException {
        verifyObject(m);
        String sql = "INSERT INTO materials (title, author, year, ISBN, idMaterialType, material_status) VALUES ('" +
                m.getTitle() + "', '" + m.getAuthor() + "', " + m.getYear() + ", '" + m.getISBN() + "', " +
                m.getIdMaterialType() + ", '" + m.getMaterial_status() + "')";
        logger.info("SQL: " + sql);

        try {
            Statement st = DAOMySQLSettings.getStatement();
            st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            var rs = st.getGeneratedKeys();
            if (rs.next()) {
                m.setIdMaterial(rs.getInt(1)); // assign the auto-generated ID
            }
            DAOMySQLSettings.closeStatement(st);
        } catch(SQLException e){
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }


    @Override
    public void update(Materialmar m) throws DAOException {
        verifyObject(m);
        String sql = "UPDATE materials SET title='" + m.getTitle() + "', author='" + m.getAuthor() +
                "', year=" + m.getYear() + ", ISBN='" + m.getISBN() +
                ", idMaterialType=" + m.getIdMaterialType() + ", material_status='" + m.getMaterial_status() +
                "' WHERE idMaterial=" + m.getIdMaterial();
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    @Override
    public void delete(Materialmar m) throws DAOException {
        if(m == null || m.getIdMaterial() == null){
            throw new DAOException("In delete: idMaterial cannot be null");
        }
        String sql = "DELETE FROM materials WHERE idMaterial=" + m.getIdMaterial();
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    private void verifyObject(Materialmar m) throws DAOException {
        if(m == null || m.getTitle() == null || m.getAuthor() == null || m.getIdMaterialType() == null
                || m.getMaterial_status() == null){
            throw new DAOException("In verifyObject: all fields must be non-null");
        }
    }

    private void executeUpdate(String sql) throws DAOException {
        try{
            Statement st = DAOMySQLSettings.getStatement();
            st.executeUpdate(sql);
            DAOMySQLSettings.closeStatement(st);
        } catch(SQLException e){
            throw new DAOException("In executeUpdate(): " + e.getMessage());
        }
    }
}
