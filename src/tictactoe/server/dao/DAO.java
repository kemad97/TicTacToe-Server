package tictactoe.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import org.apache.derby.jdbc.ClientDriver;

public class DAO {

    private static volatile DAO instance;
    private Connection con;

    private DAO() throws SQLException {
        try {
            DriverManager.registerDriver(new ClientDriver());
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/tic_tac_toe", "root", "root");
        } catch (SQLNonTransientConnectionException ex) {
            System.out.println("Database is down!");
            con = null;
        }
    }

    public static DAO getInstance() throws SQLException {
        if (instance == null) {
            return instance = new DAO();
        }
        return instance;
    }

    public static void deleteInstance() {
        instance = null;
    }

    private int getMaxID() throws SQLException {
        if (con == null) {
            instance = null;
            throw new SQLNonTransientConnectionException();
        }

        PreparedStatement ps = con.prepareStatement("SELECT MAX(id) as maxID from users");
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("maxID");
        } else {
            return 0;
        }
    }

    public int saveUser(User user) throws SQLException {
        if (con == null) {
            instance = null;
            throw new SQLNonTransientConnectionException();
        }

        PreparedStatement ps = con.prepareStatement("INSERT INTO users(id, user_name, password) VALUES (?, ?, ?)");

        ps.setInt(1, getMaxID() + 1);
        ps.setString(2, user.getUsername());
        ps.setString(3, user.getHashedPassword());

        return ps.executeUpdate();
    }

    public User getUserByUsername(String username) throws SQLException {
        if (con == null) {
            instance = null;
            throw new SQLNonTransientConnectionException();
        }

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
        if (con == null) {
            instance = null;
            throw new SQLNonTransientConnectionException();
        }

        con.close();
        instance = null;
    }

    public int getTotalPlayers() throws SQLException {
        if (con == null) {
            instance = null;
            throw new SQLNonTransientConnectionException();
        }

        int allPlayers = 0;

        String query = "SELECT COUNT(*) AS AllPlayers FROM users";

        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            allPlayers = rs.getInt("AllPlayers");
        }

        return allPlayers;
    }

    public void updateScore(User user) throws SQLException {
        if (con == null) {
            instance = null;
            throw new SQLNonTransientConnectionException();
        }

        PreparedStatement preparedStatement = con.prepareStatement("UPDATE Users SET score = ? WHERE user_name = ?");
        preparedStatement.setInt(1, user.getScore());
        preparedStatement.setString(2, user.getUsername());
        preparedStatement.executeUpdate();

    }

    public User getUserData(String username) throws SQLException {

        if (con == null) {
            instance = null;
            throw new SQLNonTransientConnectionException();
        }

        User user = null;

        PreparedStatement ps = con.prepareStatement("SELECT USER_NAME, SCORE, MATCHES_NO, WON_MATCHES FROM USERS WHERE USER_NAME = ?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            user = new User();
            user.setUsername(rs.getString("user_name"));
            user.setScore(rs.getInt("score"));
            user.setMatches_no(rs.getInt("matches_no"));
            user.setWon_matches(rs.getInt("won_matches"));
        }

        return user;
    }
    
    public void updateMatches_No(String username) throws SQLException {
        String query = "UPDATE USERS SET MATCHES_NO = MATCHES_NO + 1 WHERE USER_NAME = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    public void updateWinMatches(String username) throws SQLException {
        String query = "UPDATE USERS SET WON_MATCHES = WON_MATCHES + 1 WHERE USER_NAME = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, username);
            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("The Win Matches is update successfully for: " + username);
            } else {
                System.out.println("We can't update Win Matches for: " + username);
            }
        }
        
    }
    
    public boolean deleteUser(String username) throws SQLException {
        if(con==null)
        {
            instance=null;
            throw new SQLNonTransientConnectionException();
        }
        String query = "DELETE FROM users WHERE user_name = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) 
        {
            ps.setString(1, username);
            int result = ps.executeUpdate();

            if (result > 0) {
                System.out.println("User " + username + " deleted successfully");
                return true;
            } else {
                System.out.println("No user found with username: " + username);
                return false;
            }
        } 
        catch (SQLException e) {
            System.err.println("Error deleting user: " + username);
            e.printStackTrace();
            throw e;
        }
    
    
    }


}
    
   


