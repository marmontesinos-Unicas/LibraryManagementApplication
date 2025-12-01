package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class HoldDAOMySQLImpl implements DAO<Hold> {

    private HoldDAOMySQLImpl(){}

    private static DAO dao = null;
    private static Logger logger = null;

    public static DAO getInstance(){
        if(dao == null){
            dao = new HoldDAOMySQLImpl();
            logger = Logger.getLogger(HoldDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    @Override
    public List<Hold> select(Hold h) throws DAOException {
        if(h == null){
            h = new Hold(null, null, "", "");
        }
        ArrayList<Hold> list = new ArrayList<>();
        try{
            Statement st = DAOMySQLSettings.getStatement();
            String sql = "SELECT * FROM holds WHERE hold_date LIKE '" + h.getHold_date() + "%'";
            logger.info("SQL: " + sql);
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()){
                list.add(new Hold(
                        rs.getInt("idHold"),
                        rs.getInt("idUser"),
                        rs.getInt("idMaterial"),
                        rs.getString("hold_date"),
                        rs.getString("hold_status")
                ));
            }
            DAOMySQLSettings.closeStatement(st);
        } catch(SQLException e){
            throw new DAOException("In select(): " + e.getMessage());
        }
        return list;
    }

    @Override
    public void insert(Hold h) throws DAOException {
        verifyObject(h);
        String sql = "INSERT INTO holds (idUser, idMaterial, hold_date, hold_status) VALUES (" +
                h.getIdUser() + ", " + h.getIdMaterial() + ", '" + h.getHold_date() + "', '" +
                h.getHold_status() + "')";
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    @Override
    public void update(Hold h) throws DAOException {
        verifyObject(h);
        String sql = "UPDATE holds SET idUser=" + h.getIdUser() + ", idMaterial=" + h.getIdMaterial() +
                ", hold_date='" + h.getHold_date() + "', hold_status='" + h.getHold_status() +
                "' WHERE idHold=" + h.getIdHold();
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    @Override
    public void delete(Hold h) throws DAOException {
        if(h == null || h.getIdHold() == null){
            throw new DAOException("In delete: idHold cannot be null");
        }
        String sql = "DELETE FROM holds WHERE idHold=" + h.getIdHold();
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    private void verifyObject(Hold h) throws DAOException {
        if(h == null || h.getIdUser() == null || h.getIdMaterial() == null ||
                h.getHold_date() == null || h.getHold_status() == null){
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
