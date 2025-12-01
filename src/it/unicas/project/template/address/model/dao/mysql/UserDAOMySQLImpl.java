package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UserDAOMySQLImpl implements DAO<User> {

    protected UserDAOMySQLImpl(){}

    private static DAO dao = null;
    private static Logger logger = null;

    public static DAO getInstance(){
        if (dao == null){
            dao = new UserDAOMySQLImpl();
            logger = Logger.getLogger(UserDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    @Override
    public List<User> select(User u) throws DAOException {
        if (u == null){
            u = new User(null, "", "", "", "", "", "", null);
        }
        ArrayList<User> list = new ArrayList<>();
        try{
            if (u.getName() == null || u.getSurname() == null || u.getUsername() == null ||
                    u.getNationalID() == null || u.getPassword() == null || u.getEmail() == null){
                throw new DAOException("In select: any field cannot be null");
            }
            Statement st = DAOMySQLSettings.getStatement();
            String sql = "SELECT * FROM users WHERE surname LIKE '" + u.getSurname() + "%' " +
                    "AND name LIKE '" + u.getName() + "%' AND username LIKE '" + u.getUsername() + "%'" +
                    " AND nationalID LIKE '" + u.getNationalID() + "%' AND email LIKE '" + u.getEmail() + "%'";
            logger.info("SQL: " + sql);
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()){
                list.add(new User(
                        rs.getInt("idUser"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("username"),
                        rs.getString("nationalID"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getInt("idRole")
                ));
            }
            DAOMySQLSettings.closeStatement(st);
        } catch(SQLException e){
            throw new DAOException("In select(): " + e.getMessage());
        }
        return list;
    }

    @Override
    public void insert(User u) throws DAOException {
        verifyObject(u);
        String sql = "INSERT INTO users (name, surname, username, nationalID, password, email, idRole) VALUES ('" +
                u.getName() + "', '" + u.getSurname() + "', '" + u.getUsername() + "', '" +
                u.getNationalID() + "', '" + u.getPassword() + "', '" + u.getEmail() + "', " +
                u.getIdRole() + ")";
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    @Override
    public void update(User u) throws DAOException {
        verifyObject(u);
        String sql = "UPDATE users SET name='" + u.getName() + "', surname='" + u.getSurname() +
                "', username='" + u.getUsername() + "', nationalID='" + u.getNationalID() +
                "', password='" + u.getPassword() + "', email='" + u.getEmail() +
                "', idRole=" + u.getIdRole() + " WHERE idUser=" + u.getIdUser();
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    @Override
    public void delete(User u) throws DAOException {
        if(u == null || u.getIdUser() == null){
            throw new DAOException("In delete: idUser cannot be null");
        }
        String sql = "DELETE FROM users WHERE idUser=" + u.getIdUser();
        logger.info("SQL: " + sql);
        executeUpdate(sql);
    }

    private void verifyObject(User u) throws DAOException {
        if(u == null || u.getName() == null || u.getSurname() == null || u.getUsername() == null ||
                u.getNationalID() == null || u.getPassword() == null || u.getEmail() == null){
            throw new DAOException("In verifyObject: any field cannot be null");
        }
    }

    private void executeUpdate(String sql) throws DAOException {
        try {
            Statement st = DAOMySQLSettings.getStatement();
            st.executeUpdate(sql);
            DAOMySQLSettings.closeStatement(st);
        } catch(SQLException e){
            throw new DAOException("In executeUpdate(): " + e.getMessage());
        }
    }
}
