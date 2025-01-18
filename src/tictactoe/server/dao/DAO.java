package tictactoe.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.derby.jdbc.ClientDriver;

public class DAO {

    private static DAO instance;
    private Connection con;

    private DAO() throws SQLException {
        DriverManager.registerDriver(new ClientDriver());
        con = DriverManager.getConnection("jdbc:derby://localhost:1527/tic_tac_toe", "root", "root");

    }

    public static DAO getInstance() throws SQLException {
        if (instance == null) {
            return instance = new DAO();
        }
        return instance;
    }

    private int getMaxID() throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT MAX(id) as maxID from users");
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("maxID");
        } else {
            return 0;
        }
    }

    public int saveUser(User user) throws SQLException {
        PreparedStatement ps = con.prepareStatement("INSERT INTO users(id, user_name, password) VALUES (?, ?, ?)");

        ps.setInt(1, getMaxID() + 1);
        ps.setString(2, user.getUsername());
        ps.setString(3, user.getHashedPassword());

        return ps.executeUpdate();
    }

    public User getUserByUsername(String username) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE user_name = ?",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);

        ps.setString(1, username);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new User(rs);
        } else {
            return null;
        }

    }

    public void close() throws SQLException {
        con.close();
    }
    
    public  List<User> getAvailablePlayers() throws SQLException {
        
        List<User> availablePlayers = new ArrayList<>();
        
        PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE status = ?");
        
        ps.setInt(1, User.AVAILABLE);
        
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            
            availablePlayers.add(new User(rs));
            
        }
        
        return availablePlayers;
    }


}


