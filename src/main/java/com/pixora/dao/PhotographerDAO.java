package com.pixora.dao;

import com.pixora.model.*;
import com.pixora.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PhotographerDAO {

    public void createPendingProfile(int userId) throws SQLException {

        String sql =
                "INSERT INTO photographer_profiles" +
                        "(user_id,bio,specialty,location,approval_status,average_rating) " +
                        "VALUES(?,?,?,?, 'PENDING',0)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, "");
            ps.setString(3, "");
            ps.setString(4, "");

            ps.executeUpdate();
        }
    }


    public PhotographerProfile findProfile(int userId) throws SQLException {

        String sql =
                "SELECT * FROM photographer_profiles WHERE user_id=?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? mapProfile(rs)
                        : null;
            }
        }
    }


    public void updateProfile(
            int userId,
            String bio,
            String specialty,
            String location,
            String profileImagePath) throws SQLException {

        String sql =
                "UPDATE photographer_profiles " +
                        "SET bio=?,specialty=?,location=?, " +
                        "profile_image_path=COALESCE(?,profile_image_path), " +
                        "updated_at=SYSDATETIME() " +
                        "WHERE user_id=?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, bio);
            ps.setString(2, specialty);
            ps.setString(3, location);
            ps.setString(4, profileImagePath);
            ps.setInt(5, userId);

            ps.executeUpdate();
        }
    }


    public void setApproval(
            int userId,
            String status) throws SQLException {

        String sql =
                "UPDATE photographer_profiles " +
                        "SET approval_status=?,updated_at=SYSDATETIME() " +
                        "WHERE user_id=?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, userId);

            ps.executeUpdate();
        }
    }


    public List<User> searchApproved(
            String specialty,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate date,
            Integer minRating) throws SQLException {

        StringBuilder sql = new StringBuilder(

                "SELECT DISTINCT u.*, r.role_code " +
                        "FROM users u " +
                        "JOIN roles r ON r.role_id=u.role_id " +
                        "JOIN photographer_profiles pp ON pp.user_id=u.user_id " +
                        "WHERE r.role_code='PHOTOGRAPHER' " +
                        "AND u.status='ACTIVE' " +
                        "AND pp.approval_status='APPROVED' "
        );

        List<Object> args = new ArrayList<>();


        if (specialty != null &&
                !specialty.isBlank()) {

            sql.append(
                    "AND pp.specialty LIKE ? "
            );

            args.add(
                    "%" + specialty.trim() + "%"
            );
        }


        if (minRating != null) {

            sql.append(
                    "AND pp.average_rating>=? "
            );

            args.add(minRating);
        }


        if (minPrice != null ||
                maxPrice != null) {

            sql.append(
                    "AND EXISTS(" +
                            "SELECT 1 " +
                            "FROM photography_packages p " +
                            "WHERE p.photographer_user_id=u.user_id " +
                            "AND p.active=1 "
            );


            if (minPrice != null) {

                sql.append(
                        "AND p.price>=? "
                );

                args.add(minPrice);
            }


            if (maxPrice != null) {

                sql.append(
                        "AND p.price<=? "
                );

                args.add(maxPrice);
            }


            sql.append(") ");
        }


        if (date != null) {

            sql.append(
                    "AND EXISTS(" +
                            "SELECT 1 " +
                            "FROM availability_slots a " +
                            "WHERE a.photographer_user_id=u.user_id " +
                            "AND a.available_date=? " +
                            "AND a.active=1" +
                            ") "
            );

            args.add(
                    Date.valueOf(date)
            );
        }


        sql.append(
                "ORDER BY u.full_name"
        );


        List<User> list =
                new ArrayList<>();


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(
                             sql.toString())) {


            bind(ps, args);


            try (ResultSet rs =
                         ps.executeQuery()) {


                while (rs.next()) {

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

                    u.setFullName(
                            rs.getString("full_name")
                    );

                    u.setPhone(
                            rs.getString("phone")
                    );

                    u.setStatus(
                            rs.getString("status")
                    );


                    list.add(u);
                }
            }
        }


        return list;
    }


    public List<User> pendingPhotographers()
            throws SQLException {

        String sql =
                "SELECT u.*, r.role_code " +
                        "FROM users u " +
                        "JOIN roles r " +
                        "ON r.role_id=u.role_id " +
                        "JOIN photographer_profiles p " +
                        "ON p.user_id=u.user_id " +
                        "WHERE p.approval_status='PENDING' " +
                        "ORDER BY u.created_at";


        List<User> list =
                new ArrayList<>();


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql);

             ResultSet rs =
                     ps.executeQuery()) {


            while (rs.next()) {

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

                u.setFullName(
                        rs.getString("full_name")
                );

                u.setPhone(
                        rs.getString("phone")
                );

                u.setStatus(
                        rs.getString("status")
                );


                list.add(u);
            }
        }


        return list;
    }


    public List<PortfolioItem> portfolio(
            int photographerUserId)
            throws SQLException {

        String sql =
                "SELECT * " +
                        "FROM portfolio_items " +
                        "WHERE photographer_user_id=? " +
                        "ORDER BY created_at DESC";


        List<PortfolioItem> list =
                new ArrayList<>();


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    photographerUserId
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                while (rs.next()) {

                    PortfolioItem i =
                            new PortfolioItem();


                    i.setPortfolioId(
                            rs.getInt("portfolio_id")
                    );

                    i.setPhotographerUserId(
                            rs.getInt("photographer_user_id")
                    );

                    i.setTitle(
                            rs.getString("title")
                    );

                    i.setDescription(
                            rs.getString("description")
                    );

                    i.setImagePath(
                            rs.getString("image_path")
                    );


                    Timestamp t =
                            rs.getTimestamp("created_at");


                    if (t != null) {

                        i.setCreatedAt(
                                t.toLocalDateTime()
                        );
                    }


                    list.add(i);
                }
            }
        }


        return list;
    }


    public PortfolioItem findPortfolioItem(
            int portfolioId)
            throws SQLException {

        String sql =
                "SELECT * " +
                        "FROM portfolio_items " +
                        "WHERE portfolio_id=?";


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    portfolioId
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                if (!rs.next()) {

                    return null;
                }


                PortfolioItem i =
                        new PortfolioItem();


                i.setPortfolioId(
                        rs.getInt("portfolio_id")
                );

                i.setPhotographerUserId(
                        rs.getInt("photographer_user_id")
                );

                i.setTitle(
                        rs.getString("title")
                );

                i.setDescription(
                        rs.getString("description")
                );

                i.setImagePath(
                        rs.getString("image_path")
                );


                Timestamp t =
                        rs.getTimestamp("created_at");


                if (t != null) {

                    i.setCreatedAt(
                            t.toLocalDateTime()
                    );
                }


                return i;
            }
        }
    }


    public void updatePortfolio(
            int portfolioId,
            int photographerUserId,
            String title,
            String description)
            throws SQLException {

        String sql =
                "UPDATE portfolio_items " +
                        "SET title=?,description=? " +
                        "WHERE portfolio_id=? " +
                        "AND photographer_user_id=?";


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setString(
                    1,
                    title
            );

            ps.setString(
                    2,
                    description
            );

            ps.setInt(
                    3,
                    portfolioId
            );

            ps.setInt(
                    4,
                    photographerUserId
            );


            if (ps.executeUpdate() != 1) {

                throw new SQLException(
                        "Portfolio item not found."
                );
            }
        }
    }


    public void addPortfolio(
            int photographerUserId,
            String title,
            String description,
            String imagePath)
            throws SQLException {

        String sql =
                "INSERT INTO portfolio_items" +
                        "(photographer_user_id,title,description,image_path) " +
                        "VALUES(?,?,?,?)";


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    photographerUserId
            );

            ps.setString(
                    2,
                    title
            );

            ps.setString(
                    3,
                    description
            );

            ps.setString(
                    4,
                    imagePath
            );


            ps.executeUpdate();
        }
    }


    public String deletePortfolio(
            int portfolioId,
            int photographerUserId)
            throws SQLException {

        String path =
                null;


        try (Connection c =
                     DBConnection.getConnection()) {


            c.setAutoCommit(false);


            try (PreparedStatement find =
                         c.prepareStatement(
                                 "SELECT image_path " +
                                         "FROM portfolio_items " +
                                         "WHERE portfolio_id=? " +
                                         "AND photographer_user_id=?")) {


                find.setInt(
                        1,
                        portfolioId
                );

                find.setInt(
                        2,
                        photographerUserId
                );


                try (ResultSet rs =
                             find.executeQuery()) {

                    if (rs.next()) {

                        path =
                                rs.getString(1);
                    }
                }
            }


            try (PreparedStatement del =
                         c.prepareStatement(
                                 "DELETE FROM portfolio_items " +
                                         "WHERE portfolio_id=? " +
                                         "AND photographer_user_id=?")) {


                del.setInt(
                        1,
                        portfolioId
                );

                del.setInt(
                        2,
                        photographerUserId
                );


                del.executeUpdate();
            }


            c.commit();
        }


        return path;
    }


    public List<PhotographyPackage> packages(
            int photographerUserId,
            boolean activeOnly)
            throws SQLException {

        String sql =
                "SELECT * " +
                        "FROM photography_packages " +
                        "WHERE photographer_user_id=? " +
                        (activeOnly
                                ? "AND active=1 "
                                : "") +
                        "ORDER BY price";


        List<PhotographyPackage> list =
                new ArrayList<>();


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    photographerUserId
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                while (rs.next()) {

                    list.add(
                            mapPackage(rs)
                    );
                }
            }
        }


        return list;
    }


    public PhotographyPackage findPackage(
            int packageId)
            throws SQLException {

        String sql =
                "SELECT * " +
                        "FROM photography_packages " +
                        "WHERE package_id=?";


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    packageId
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                return rs.next()
                        ? mapPackage(rs)
                        : null;
            }
        }
    }


    public void savePackage(
            int photographerUserId,
            int packageId,
            String name,
            String description,
            BigDecimal price,
            int durationHours)
            throws SQLException {


        if (packageId > 0) {

            String sql =
                    "UPDATE photography_packages " +
                            "SET name=?,description=?,price=?," +
                            "duration_hours=?,updated_at=SYSDATETIME() " +
                            "WHERE package_id=? " +
                            "AND photographer_user_id=?";


            try (Connection c =
                         DBConnection.getConnection();

                 PreparedStatement ps =
                         c.prepareStatement(sql)) {


                ps.setString(
                        1,
                        name
                );

                ps.setString(
                        2,
                        description
                );

                ps.setBigDecimal(
                        3,
                        price
                );

                ps.setInt(
                        4,
                        durationHours
                );

                ps.setInt(
                        5,
                        packageId
                );

                ps.setInt(
                        6,
                        photographerUserId
                );


                ps.executeUpdate();
            }


        } else {


            String sql =
                    "INSERT INTO photography_packages" +
                            "(photographer_user_id,name,description,price,duration_hours,active) " +
                            "VALUES(?,?,?,?,?,1)";


            try (Connection c =
                         DBConnection.getConnection();

                 PreparedStatement ps =
                         c.prepareStatement(sql)) {


                ps.setInt(
                        1,
                        photographerUserId
                );

                ps.setString(
                        2,
                        name
                );

                ps.setString(
                        3,
                        description
                );

                ps.setBigDecimal(
                        4,
                        price
                );

                ps.setInt(
                        5,
                        durationHours
                );


                ps.executeUpdate();
            }
        }
    }


    public void deactivatePackage(
            int packageId,
            int photographerUserId)
            throws SQLException {

        String sql =
                "UPDATE photography_packages " +
                        "SET active=0,updated_at=SYSDATETIME() " +
                        "WHERE package_id=? " +
                        "AND photographer_user_id=?";


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    packageId
            );

            ps.setInt(
                    2,
                    photographerUserId
            );


            ps.executeUpdate();
        }
    }


    /*
     * =========================================================
     * GET PHOTOGRAPHER AVAILABILITY
     * =========================================================
     */

    public List<AvailabilitySlot> availability(
            int photographerUserId,
            boolean futureOnly)
            throws SQLException {

        String sql =
                "SELECT * " +
                        "FROM availability_slots " +
                        "WHERE photographer_user_id=? " +

                        (futureOnly
                                ? "AND available_date>=CAST(GETDATE() AS date) "
                                : "") +

                        "ORDER BY available_date,start_time";


        List<AvailabilitySlot> list =
                new ArrayList<>();


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    photographerUserId
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                while (rs.next()) {


                    AvailabilitySlot a =
                            new AvailabilitySlot();


                    a.setAvailabilityId(
                            rs.getInt(
                                    "availability_id"
                            )
                    );


                    a.setPhotographerUserId(
                            rs.getInt(
                                    "photographer_user_id"
                            )
                    );


                    Date availableDate =
                            rs.getDate(
                                    "available_date"
                            );


                    if (availableDate != null) {

                        a.setAvailableDate(
                                availableDate.toLocalDate()
                        );
                    }


                    Time startTime =
                            rs.getTime(
                                    "start_time"
                            );


                    if (startTime != null) {

                        a.setStartTime(
                                startTime.toLocalTime()
                        );
                    }


                    Time endTime =
                            rs.getTime(
                                    "end_time"
                            );


                    if (endTime != null) {

                        a.setEndTime(
                                endTime.toLocalTime()
                        );
                    }


                    a.setActive(
                            rs.getBoolean(
                                    "active"
                            )
                    );


                    list.add(a);
                }
            }
        }


        return list;
    }


    /*
     * =========================================================
     * NEW METHOD
     *
     * GET AVAILABILITY FOR ONE SELECTED DATE
     * =========================================================
     */

    public List<AvailabilitySlot> availabilityForDate(
            int photographerUserId,
            LocalDate date)
            throws SQLException {


        String sql =
                "SELECT * " +
                        "FROM availability_slots " +
                        "WHERE photographer_user_id=? " +
                        "AND available_date=? " +
                        "AND active=1 " +
                        "ORDER BY start_time";


        List<AvailabilitySlot> list =
                new ArrayList<>();


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    photographerUserId
            );


            ps.setDate(
                    2,
                    Date.valueOf(date)
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                while (rs.next()) {


                    AvailabilitySlot a =
                            new AvailabilitySlot();


                    a.setAvailabilityId(
                            rs.getInt(
                                    "availability_id"
                            )
                    );


                    a.setPhotographerUserId(
                            rs.getInt(
                                    "photographer_user_id"
                            )
                    );


                    Date availableDate =
                            rs.getDate(
                                    "available_date"
                            );


                    if (availableDate != null) {

                        a.setAvailableDate(
                                availableDate.toLocalDate()
                        );
                    }


                    Time startTime =
                            rs.getTime(
                                    "start_time"
                            );


                    if (startTime != null) {

                        a.setStartTime(
                                startTime.toLocalTime()
                        );
                    }


                    Time endTime =
                            rs.getTime(
                                    "end_time"
                            );


                    if (endTime != null) {

                        a.setEndTime(
                                endTime.toLocalTime()
                        );
                    }


                    a.setActive(
                            rs.getBoolean(
                                    "active"
                            )
                    );


                    list.add(a);
                }
            }
        }


        return list;
    }


    public AvailabilitySlot findAvailability(
            int id)
            throws SQLException {

        String sql =
                "SELECT * " +
                        "FROM availability_slots " +
                        "WHERE availability_id=?";


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    id
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                if (!rs.next()) {

                    return null;
                }


                AvailabilitySlot a =
                        new AvailabilitySlot();


                a.setAvailabilityId(
                        rs.getInt(
                                "availability_id"
                        )
                );


                a.setPhotographerUserId(
                        rs.getInt(
                                "photographer_user_id"
                        )
                );


                Date availableDate =
                        rs.getDate(
                                "available_date"
                        );


                if (availableDate != null) {

                    a.setAvailableDate(
                            availableDate.toLocalDate()
                    );
                }


                Time startTime =
                        rs.getTime(
                                "start_time"
                        );


                if (startTime != null) {

                    a.setStartTime(
                            startTime.toLocalTime()
                    );
                }


                Time endTime =
                        rs.getTime(
                                "end_time"
                        );


                if (endTime != null) {

                    a.setEndTime(
                            endTime.toLocalTime()
                    );
                }


                a.setActive(
                        rs.getBoolean(
                                "active"
                        )
                );


                return a;
            }
        }
    }


    public void updateAvailability(
            int id,
            int photographerUserId,
            LocalDate date,
            LocalTime start,
            LocalTime end)
            throws SQLException {


        String conflict =
                "SELECT COUNT(*) " +
                        "FROM availability_slots " +
                        "WHERE photographer_user_id=? " +
                        "AND availability_id<>? " +
                        "AND available_date=? " +
                        "AND active=1 " +
                        "AND start_time<? " +
                        "AND end_time>?";


        try (Connection c =
                     DBConnection.getConnection()) {


            try (PreparedStatement check =
                         c.prepareStatement(conflict)) {


                check.setInt(
                        1,
                        photographerUserId
                );

                check.setInt(
                        2,
                        id
                );

                check.setDate(
                        3,
                        Date.valueOf(date)
                );

                check.setTime(
                        4,
                        Time.valueOf(end)
                );

                check.setTime(
                        5,
                        Time.valueOf(start)
                );


                try (ResultSet rs =
                             check.executeQuery()) {


                    if (rs.next() &&
                            rs.getInt(1) > 0) {


                        throw new SQLException(
                                "Availability overlaps an existing slot."
                        );
                    }
                }
            }


            String updateSql =
                    "UPDATE availability_slots " +
                            "SET available_date=?,start_time=?,end_time=?,active=1 " +
                            "WHERE availability_id=? " +
                            "AND photographer_user_id=?";


            try (PreparedStatement ps =
                         c.prepareStatement(updateSql)) {


                ps.setDate(
                        1,
                        Date.valueOf(date)
                );

                ps.setTime(
                        2,
                        Time.valueOf(start)
                );

                ps.setTime(
                        3,
                        Time.valueOf(end)
                );

                ps.setInt(
                        4,
                        id
                );

                ps.setInt(
                        5,
                        photographerUserId
                );


                if (ps.executeUpdate() != 1) {

                    throw new SQLException(
                            "Availability slot not found."
                    );
                }
            }
        }
    }


    public void addAvailability(
            int photographerUserId,
            LocalDate date,
            LocalTime start,
            LocalTime end)
            throws SQLException {


        String conflict =
                "SELECT COUNT(*) " +
                        "FROM availability_slots " +
                        "WHERE photographer_user_id=? " +
                        "AND available_date=? " +
                        "AND active=1 " +
                        "AND start_time<? " +
                        "AND end_time>?";


        try (Connection c =
                     DBConnection.getConnection()) {


            try (PreparedStatement check =
                         c.prepareStatement(conflict)) {


                check.setInt(
                        1,
                        photographerUserId
                );

                check.setDate(
                        2,
                        Date.valueOf(date)
                );

                check.setTime(
                        3,
                        Time.valueOf(end)
                );

                check.setTime(
                        4,
                        Time.valueOf(start)
                );


                try (ResultSet rs =
                             check.executeQuery()) {


                    if (rs.next() &&
                            rs.getInt(1) > 0) {


                        throw new SQLException(
                                "Availability overlaps an existing slot."
                        );
                    }
                }
            }


            String insertSql =
                    "INSERT INTO availability_slots" +
                            "(photographer_user_id,available_date,start_time,end_time,active) " +
                            "VALUES(?,?,?,?,1)";


            try (PreparedStatement ps =
                         c.prepareStatement(insertSql)) {


                ps.setInt(
                        1,
                        photographerUserId
                );

                ps.setDate(
                        2,
                        Date.valueOf(date)
                );

                ps.setTime(
                        3,
                        Time.valueOf(start)
                );

                ps.setTime(
                        4,
                        Time.valueOf(end)
                );


                ps.executeUpdate();
            }
        }
    }


    public void removeAvailability(
            int id,
            int photographerUserId)
            throws SQLException {

        String sql =
                "UPDATE availability_slots " +
                        "SET active=0 " +
                        "WHERE availability_id=? " +
                        "AND photographer_user_id=?";


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    id
            );

            ps.setInt(
                    2,
                    photographerUserId
            );


            ps.executeUpdate();
        }
    }


    public boolean isAvailable(
            int photographerUserId,
            LocalDate date,
            LocalTime start,
            LocalTime end)
            throws SQLException {


        String sql =
                "SELECT COUNT(*) " +
                        "FROM availability_slots " +
                        "WHERE photographer_user_id=? " +
                        "AND available_date=? " +
                        "AND active=1 " +
                        "AND start_time<=? " +
                        "AND end_time>=?";


        try (Connection c =
                     DBConnection.getConnection();

             PreparedStatement ps =
                     c.prepareStatement(sql)) {


            ps.setInt(
                    1,
                    photographerUserId
            );

            ps.setDate(
                    2,
                    Date.valueOf(date)
            );

            ps.setTime(
                    3,
                    Time.valueOf(start)
            );

            ps.setTime(
                    4,
                    Time.valueOf(end)
            );


            try (ResultSet rs =
                         ps.executeQuery()) {


                return rs.next() &&
                        rs.getInt(1) > 0;
            }
        }
    }


    private PhotographerProfile mapProfile(
            ResultSet rs)
            throws SQLException {


        PhotographerProfile p =
                new PhotographerProfile();


        p.setProfileId(
                rs.getInt(
                        "profile_id"
                )
        );


        p.setUserId(
                rs.getInt(
                        "user_id"
                )
        );


        p.setBio(
                rs.getString(
                        "bio"
                )
        );


        p.setSpecialty(
                rs.getString(
                        "specialty"
                )
        );


        p.setLocation(
                rs.getString(
                        "location"
                )
        );


        p.setApprovalStatus(
                rs.getString(
                        "approval_status"
                )
        );


        p.setAverageRating(
                rs.getBigDecimal(
                        "average_rating"
                )
        );


        p.setProfileImagePath(
                rs.getString(
                        "profile_image_path"
                )
        );


        Timestamp t =
                rs.getTimestamp(
                        "created_at"
                );


        if (t != null) {

            p.setCreatedAt(
                    t.toLocalDateTime()
            );
        }


        return p;
    }


    private PhotographyPackage mapPackage(
            ResultSet rs)
            throws SQLException {


        PhotographyPackage p =
                new PhotographyPackage();


        p.setPackageId(
                rs.getInt(
                        "package_id"
                )
        );


        p.setPhotographerUserId(
                rs.getInt(
                        "photographer_user_id"
                )
        );


        p.setName(
                rs.getString(
                        "name"
                )
        );


        p.setDescription(
                rs.getString(
                        "description"
                )
        );


        p.setPrice(
                rs.getBigDecimal(
                        "price"
                )
        );


        p.setDurationHours(
                rs.getInt(
                        "duration_hours"
                )
        );


        p.setActive(
                rs.getBoolean(
                        "active"
                )
        );


        return p;
    }


    private void bind(
            PreparedStatement ps,
            List<Object> args)
            throws SQLException {


        for (int i = 0;
             i < args.size();
             i++) {


            Object o =
                    args.get(i);

            int idx =
                    i + 1;


            if (o instanceof Date) {

                ps.setDate(
                        idx,
                        (Date) o
                );


            } else if (o instanceof BigDecimal) {

                ps.setBigDecimal(
                        idx,
                        (BigDecimal) o
                );


            } else if (o instanceof Integer) {

                ps.setInt(
                        idx,
                        (Integer) o
                );


            } else {

                ps.setString(
                        idx,
                        String.valueOf(o)
                );
            }
        }
    }
}