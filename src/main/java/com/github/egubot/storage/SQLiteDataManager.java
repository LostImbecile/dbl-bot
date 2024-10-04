package com.github.egubot.storage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.javacord.api.entity.message.Messageable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SQLiteDataManager extends BaseDataManager {
    private Connection conn;
    private Gson gson;

    public SQLiteDataManager(String dataName) throws IOException {
        super(dataName);
        this.filePath = STORAGE_FOLDER + File.separator +  dataName.replace(" ", "_") + ".db";
		this.fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
		
        this.gson = new Gson();
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + filePath);
            initializeTable();
        } catch (SQLException e) {
            throw new IOException("Failed to initialize SQLite database", e);
        }
    }

    private void initializeTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS data (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "content TEXT NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public void initialise(boolean verbose) throws IOException {
        readData();
        if (verbose) {
            System.out.println(dataName + " data successfully loaded!");
        }
    }

    @Override
    public void writeData(Messageable e) {
        try {
            String json = gson.toJson(data);
            String sql = "INSERT OR REPLACE INTO data(id, content) VALUES(1, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, json);
                pstmt.executeUpdate();
            }
            if (e != null) {
                e.sendMessage("Updated <:drink:1184466286944735272>");
            }
        } catch (SQLException ex) {
            if (e != null) {
                e.sendMessage("Couldn't update <:sad:1020780174901522442>");
            }
            ex.printStackTrace();
        }
    }

    @Override
    public void readData() {
        try {
            String sql = "SELECT content FROM data WHERE id = 1";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    String json = rs.getString("content");
                    data = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
                } else {
                    data = new ArrayList<>();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sendData(Messageable e) {
        if (data != null) {
            e.sendMessage(String.join("\n", data));
        } else {
            e.sendMessage("No data available.");
        }
    }

    @Override
    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <T> void storeObject(T object, String key) {
        String json = gson.toJson(object);
        try {
            String sql = "INSERT OR REPLACE INTO data(id, content) VALUES(?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, key);
                pstmt.setString(2, json);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <T> T retrieveObject(String key, Class<T> type) {
        try {
            String sql = "SELECT content FROM data WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, key);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String json = rs.getString("content");
                        return gson.fromJson(json, type);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void writeJSON(String key, Object object) {
        try {
            String json = gson.toJson(object);
            String sql = "INSERT OR REPLACE INTO data(id, content) VALUES(?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, key);
                pstmt.setBytes(2, json.getBytes());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <T> T readJSON(String key, Class<T> type) {
        try {
            String sql = "SELECT content FROM data WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, key);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        byte[] bytes = rs.getBytes("content");
                        String json = new String(bytes);
                        return gson.fromJson(json, type);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> List<T> readJSONList(String key, TypeToken<List<T>> typeToken) {
        try {
            String sql = "SELECT content FROM data WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, key);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        byte[] bytes = rs.getBytes("content");
                        String json = new String(bytes);
                        return gson.fromJson(json, typeToken.getType());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}