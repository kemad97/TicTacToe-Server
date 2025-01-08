package tictactoe.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.derby.jdbc.ClientDriver;

public class DAO {

    private static Connection con;

    static {
        try {
            DriverManager.registerDriver(new ClientDriver());
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/tic_tac_toe", "root", "root");
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static int getMaxID() throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT MAX(id) as maxID from users");
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("maxID");
        } else {
            return 0;
        }
    }

    public static int saveUser(User user) throws SQLException {
        PreparedStatement ps = con.prepareStatement("INSERT INTO users(id, user_name, password) VALUES (?, ?, ?)");
        
        ps.setInt(1, getMaxID()+1);
        ps.setString(2, user.getUsername());
        ps.setString(3, user.getHashedPassword());        

        return ps.executeUpdate();
    }

    public static void close() throws SQLException {
        con.close();
    }

}
