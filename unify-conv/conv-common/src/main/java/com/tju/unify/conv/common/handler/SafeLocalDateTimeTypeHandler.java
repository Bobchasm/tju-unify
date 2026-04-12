package com.tju.unify.conv.common.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@MappedTypes(LocalDateTime.class)
public class SafeLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setTimestamp(i, Timestamp.valueOf(parameter));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            // 优先尝试 getTimestamp
            Timestamp timestamp = rs.getTimestamp(columnName);
            if (timestamp != null) {
                return timestamp.toLocalDateTime();
            }
        } catch (Exception e) {
            // 如果失败，尝试 getString
            try {
                String dateStr = rs.getString(columnName);
                if (dateStr != null && !dateStr.isEmpty()) {
                    return LocalDateTime.parse(dateStr, FORMATTER);
                }
            } catch (Exception ex) {
                // 忽略，返回null
            }
        }
        return null;
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            Timestamp timestamp = rs.getTimestamp(columnIndex);
            if (timestamp != null) {
                return timestamp.toLocalDateTime();
            }
        } catch (Exception e) {
            try {
                String dateStr = rs.getString(columnIndex);
                if (dateStr != null && !dateStr.isEmpty()) {
                    return LocalDateTime.parse(dateStr, FORMATTER);
                }
            } catch (Exception ex) {
                // 忽略
            }
        }
        return null;
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}