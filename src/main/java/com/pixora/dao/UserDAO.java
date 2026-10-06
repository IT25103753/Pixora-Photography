package com.pixora.dao;

import com.pixora.model.User;
import com.pixora.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /* =====================================================
       NORMAL LOGIN
       ===================================================== */

    public User findByLogin(String login)
            throws SQLException {

        String sql =
                "SELECT u.*, r.role_code " +
                        "FROM users u " +
                        "JOIN roles r ON r.role_id=u.role_id " +
                        "WHERE (u.username=? OR u.email=?)";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setString(1, login);
            ps.setString(2, login);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? map(rs)
                        : null;
            }
        }
    }

    /* =====================================================
       GOOGLE LOGIN
       ===================================================== */

    public User findByEmail(String email)
            throws SQLException {

        String sql =
                "SELECT u.*, r.role_code " +
                        "FROM users u " +
                        "JOIN roles r ON r.role_id=u.role_id " +
                        "WHERE LOWER(u.email)=LOWER(?)";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? map(rs)
                        : null;
            }
        }
    }

    public User findByGoogleSub(String googleSub)
            throws SQLException {

        String sql =
                "SELECT u.*, r.role_code " +
                        "FROM users u " +
                        "JOIN roles r ON r.role_id=u.role_id " +
                        "WHERE u.google_sub=?";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    googleSub
            );

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? map(rs)
                        : null;
            }
        }
    }

    /*
     * Existing local PIXORA account +
     * verified Google account =
     * BOTH authentication methods.
     */
    public void linkGoogleAccount(
            int userId,
            String googleSub
    ) throws SQLException {

        String sql =
                "UPDATE users " +
                        "SET google_sub=?, " +
                        "auth_provider=" +
                        "CASE " +
                        "WHEN auth_provider='LOCAL' THEN 'BOTH' " +
                        "ELSE auth_provider " +
                        "END, " +
                        "updated_at=SYSDATETIME() " +
                        "WHERE user_id=?";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    googleSub
            );

            ps.setInt(
                    2,
                    userId
            );

            ps.executeUpdate();
        }
    }

    /*
     * IMPORTANT:
     *
     * Google self-registration creates CUSTOMER accounts only.
     *
     * Photographers must still use PIXORA's photographer
     * registration because they require administrator approval.
     */
    public int createGoogleCustomer(
            String username,
            String email,
            String fullName,
            String googleSub
    ) throws SQLException {

        String sql =
                "INSERT INTO users(" +
                        "role_id," +
                        "username," +
                        "email," +
                        "password_hash," +
                        "full_name," +
                        "phone," +
                        "status," +
                        "auth_provider," +
                        "google_sub" +
                        ") " +

                        "VALUES(" +
                        "(SELECT role_id FROM roles " +
                        " WHERE role_code='CUSTOMER')," +
                        "?,?," +
                        "NULL," +
                        "?," +
                        "NULL," +
                        "'ACTIVE'," +
                        "'GOOGLE'," +
                        "?" +
                        ")";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            ps.setString(
                    1,
                    username
            );

            ps.setString(
                    2,
                    email
            );

            ps.setString(
                    3,
                    fullName
            );

            ps.setString(
                    4,
                    googleSub
            );

            ps.executeUpdate();

            try (
                    ResultSet keys =
                            ps.getGeneratedKeys()
            ) {

                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Google user could not be created."
        );
    }

    /*
     * Creates a PIXORA username automatically
     * from the Google email.
     *
     * Example:
     *
     * naveen@gmail.com
     *
     * becomes
     *
     * naveen
     *
     * If naveen already exists:
     *
     * naveen1
     * naveen2
     * ...
     */
    public String generateGoogleUsername(
            String email
    ) throws SQLException {

        String local;

        if (email == null
                || !email.contains("@")) {

            local = "pixora";

        } else {

            local =
                    email.substring(
                            0,
                            email.indexOf('@')
                    );
        }

        String base =
                local.toLowerCase()
                        .replaceAll(
                                "[^a-z0-9._]",
                                ""
                        )
                        .replaceAll(
                                "^[._]+|[._]+$",
                                ""
                        );

        if (base.isBlank()) {
            base = "pixora";
        }

        if (base.length() > 45) {

            base =
                    base.substring(
                            0,
                            45
                    );
        }

        String candidate = base;

        int suffix = 1;

        while (usernameExists(candidate)) {

            candidate =
                    base + suffix;

            suffix++;
        }

        return candidate;
    }

    private boolean usernameExists(
            String username
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM users " +
                        "WHERE username=?";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    username
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next()
                        && rs.getInt(1) > 0;
            }
        }
    }

    /* =====================================================
       EXISTING PIXORA METHODS
       ===================================================== */

    public User findById(int id)
            throws SQLException {

        String sql =
                "SELECT u.*, r.role_code " +
                        "FROM users u " +
                        "JOIN roles r ON r.role_id=u.role_id " +
                        "WHERE u.user_id=?";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    id
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next()
                        ? map(rs)
                        : null;
            }
        }
    }

    public boolean existsUsernameOrEmail(
            String username,
            String email
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM users " +
                        "WHERE username=? OR email=?";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    username
            );

            ps.setString(
                    2,
                    email
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next()
                        && rs.getInt(1) > 0;
            }
        }
    }

    public int create(
            String roleCode,
            String username,
            String email,
            String passwordHash,
            String fullName,
            String phone
    ) throws SQLException {

        String sql =
                "INSERT INTO users(" +
                        "role_id," +
                        "username," +
                        "email," +
                        "password_hash," +
                        "full_name," +
                        "phone," +
                        "status" +
                        ") " +

                        "VALUES(" +
                        "(SELECT role_id FROM roles " +
                        "WHERE role_code=?)," +
                        "?,?,?,?,?," +
                        "'ACTIVE'" +
                        ")";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            ps.setString(
                    1,
                    roleCode
            );

            ps.setString(
                    2,
                    username
            );

            ps.setString(
                    3,
                    email
            );

            ps.setString(
                    4,
                    passwordHash
            );

            ps.setString(
                    5,
                    fullName
            );

            ps.setString(
                    6,
                    phone
            );

            ps.executeUpdate();

            try (
                    ResultSet keys =
                            ps.getGeneratedKeys()
            ) {

                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException(
                "User could not be created."
        );
    }

    public void updateOwnProfile(
            int userId,
            String fullName,
            String email,
            String phone
    ) throws SQLException {

        if (emailUsedByOther(
                userId,
                email
        )) {

            throw new SQLException(
                    "That email address is already in use."
            );
        }

        String sql =
                "UPDATE users " +
                        "SET full_name=?," +
                        "email=?," +
                        "phone=?," +
                        "updated_at=SYSDATETIME() " +
                        "WHERE user_id=?";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    fullName
            );

            ps.setString(
                    2,
                    email
            );

            ps.setString(
                    3,
                    phone
            );

            ps.setInt(
                    4,
                    userId
            );

            ps.executeUpdate();
        }
    }

    private boolean emailUsedByOther(
            int userId,
            String email
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM users " +
                        "WHERE email=? " +
                        "AND user_id<>?";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    email
            );

            ps.setInt(
                    2,
                    userId
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next()
                        && rs.getInt(1) > 0;
            }
        }
    }

    public void updateStatus(
            int userId,
            String status
    ) throws SQLException {

        String sql =
                "UPDATE users " +
                        "SET status=?," +
                        "updated_at=SYSDATETIME() " +
                        "WHERE user_id=?";

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    status
            );

            ps.setInt(
                    2,
                    userId
            );

            ps.executeUpdate();
        }
    }

    public List<User> findAll()
            throws SQLException {

        String sql =
                "SELECT u.*, r.role_code " +
                        "FROM users u " +
                        "JOIN roles r " +
                        "ON r.role_id=u.role_id " +
                        "ORDER BY u.created_at DESC";

        List<User> users =
                new ArrayList<>();

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                users.add(
                        map(rs)
                );
            }
        }

        return users;
    }

    public List<User> findByRole(
            String roleCode
    ) throws SQLException {

        String sql =
                "SELECT u.*, r.role_code " +
                        "FROM users u " +
                        "JOIN roles r " +
                        "ON r.role_id=u.role_id " +
                        "WHERE r.role_code=? " +
                        "AND u.status='ACTIVE' " +
                        "ORDER BY u.full_name";

        List<User> users =
                new ArrayList<>();

        try (
                Connection c =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        c.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    roleCode
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {

                    users.add(
                            map(rs)
                    );
                }
            }
        }

        return users;
    }

    /* =====================================================
       RESULT SET -> USER
       ===================================================== */

    private User map(ResultSet rs)
            throws SQLException {

        User u =
                new User();

        u.setUserId(
                rs.getInt("user_id")
        );

        u.setRoleId(
                rs.getInt("role_id")
        );

        u.setRoleCode(
                rs.getString("role_code")
        );

        u.setUsername(
                rs.getString("username")
        );

        u.setEmail(
                rs.getString("email")
        );

        u.setPasswordHash(
                rs.getString("password_hash")
        );

        u.setFullName(
                rs.getString("full_name")
        );

        u.setPhone(
                rs.getString("phone")
        );

        u.setStatus(
                rs.getString("status")
        );

        u.setAuthProvider(
                rs.getString("auth_provider")
        );

        u.setGoogleSub(
                rs.getString("google_sub")
        );

        Timestamp created =
                rs.getTimestamp("created_at");

        Timestamp modified =
                rs.getTimestamp("updated_at");

        if (created != null) {

            u.setCreatedAt(
                    created.toLocalDateTime()
            );
        }

        if (modified != null) {

            u.setUpdatedAt(
                    modified.toLocalDateTime()
            );
        }

        return u;
    }
}