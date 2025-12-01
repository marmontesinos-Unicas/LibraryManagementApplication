package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LoanDAOMySQLImpl implements DAO<Loan> {

    private LoanDAOMySQLImpl(){}

    private static DAO dao = null;
    private static Logger logger = null;

    public static DAO getInstance(){
        if(dao == null){
            dao = new LoanDAOMySQLImpl();
            logger = Logger.getLogger(LoanDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    @Override
    public List<Loan> select(Loan l) throws DAOException {
        if(l == null){
            l = new Loan(null, null, "", "", "");
        }
        ArrayList<Loan> list = new ArrayList<>();
        try{
            Statement st = DAOMySQLSettings.getStatement();
            String sql = "SELECT * FROM loans WHERE start_date LIKE '" + l.getStart_date() + "%'";
            logger.info("SQL: " + sql);
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()){
                list.add(new Loan(
                        rs.getInt("idLoan"),
                        rs.getInt("idUser"),
                        rs.getInt("idMaterial"),
                        rs.getString("start_date"),
                        rs.getString("due_date"),
                        rs.getString("return_date")
                ));
            }
            DAOMySQLSettings.closeStatement(st);
        } catch(SQLException e){
            throw new DAOException("In select(): " + e.getMessage());
        }
        return list;
    }

    @Override
    public void insert(Loan l) throws DAOException {
        verifyObject(l);
        String sql = "INSERT INTO loans (idUser, idMaterial, start_date, due_date, return_date) VALUES (" +
                l.getIdUser() + ", " + l.getIdMaterial() + ", '" + l.getStart_date() + "', '" +
                l.getDue_date() + "', '" + l.getReturn_date() + "')";
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    @Override
    public void update(Loan l) throws DAOException {
        verifyObject(l);
        String sql = "UPDATE loans SET idUser=" + l.getIdUser() + ", idMaterial=" + l.getIdMaterial() +
                ", start_date='" + l.getStart_date() + "', due_date='" + l.getDue_date() +
                "', return_date='" + l.getReturn_date() + "' WHERE idLoan=" + l.getIdLoan();
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    @Override
    public void delete(Loan l) throws DAOException {
        if(l == null || l.getIdLoan() == null){
            throw new DAOException("In delete: idLoan cannot be null");
        }
        String sql = "DELETE FROM loans WHERE idLoan=" + l.getIdLoan();
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    private void verifyObject(Loan l) throws DAOException {
        if(l == null || l.getIdUser() == null || l.getIdMaterial() == null ||
                l.getStart_date() == null || l.getDue_date() == null){
            throw new DAOException("In verifyObject: required fields must be non-null");
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
