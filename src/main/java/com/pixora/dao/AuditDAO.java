package com.pixora.dao;

import com.pixora.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AuditDAO {
    public void log(Integer userId, String actionType, String entityType, Integer entityId, String details) {
        String sql = "INSERT INTO audit_logs(user_id, action_type, entity_type, entity_id, details) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId == null) ps.setNull(1, java.sql.Types.INTEGER); else ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, entityType);
            if (entityId == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, entityId);
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (Exception ignored) {
            // Auditing should not hide the original business operation if the audit write itself fails.
        }
    }
}
