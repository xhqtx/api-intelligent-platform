package com.example.auth.config.p6spy;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomLineFormat implements MessageFormattingStrategy {

    private static final String SQL_PATTERN = "\\s+";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }
        
        // 格式化SQL
        String formatSql = formatSql(category, sql);
        
        // 当前时间
        String currentDate = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        
        return String.format("[P6Spy SQL] %s | %s ms | %s", 
                currentDate, elapsed, formatSql);
    }

    private String formatSql(String category, String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }
        
        // 只格式化查询语句
        if (category.contains("statement")) {
            String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);
            if (trimmedSQL.startsWith("select") || 
                trimmedSQL.startsWith("insert") || 
                trimmedSQL.startsWith("update") || 
                trimmedSQL.startsWith("delete")) {
                try {
                    // 使用Hibernate的格式化工具
                    sql = FormatStyle.BASIC.getFormatter().format(sql);
                } catch (Exception e) {
                    // 如果格式化失败，使用简单的空白压缩
                    sql = sql.replaceAll(SQL_PATTERN, " ");
                }
            }
        }
        
        return sql;
    }
}