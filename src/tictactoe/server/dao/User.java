package tictactoe.server.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {

    public static final int NOT_AVAILABLE = 1;//logged in 
    public static final int AVAILABLE = 2;//waiting for requset to play
    public static final int IN_GAME = 3;//playing with another

    private Integer id;
    private String username;
    private String hashedPassword;
    private Integer score;
    private int status;
    private String avatar;
    private Integer matches_no;
    private Integer won_matches;
    private String email;

    public User() {
        score = 0;
        status = AVAILABLE;
        matches_no = 0;
        won_matches = 0;
    }

    public User(String username, String password) {
        this.username = username;
        this.hashedPassword = password;
        status = AVAILABLE;
    }

    User(ResultSet rs) throws SQLException {
        this.id = rs.getInt("id");
        this.username = rs.getString("user_name");
        this.hashedPassword = rs.getString("password");
        this.score = rs.getInt("score");
        this.status = AVAILABLE;
        this.avatar = rs.getString("avatar");
        this.matches_no = rs.getInt("matches_no");
        this.won_matches = rs.getInt("won_matches");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return this.hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getMatches_no() {
        return matches_no;
    }

    public void setMatches_no(Integer matches_no) {
        this.matches_no = matches_no;
    }

    public Integer getWon_matches() {
        return won_matches;
    }

    public void setWon_matches(Integer won_matches) {
        this.won_matches = won_matches;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}


