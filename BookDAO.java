package com.library.dao;

import com.library.config.Database;
import com.library.model.Book;
import java.sql.*;
import java.util.*;

public class BookDAO {
    public List<Book> findAll(String search) throws SQLException {
        String sql = """
            SELECT * FROM books
            WHERE title LIKE ? OR author LIKE ? OR category LIKE ? OR isbn LIKE ?
            ORDER BY id DESC
            """;
        List<Book> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String q = "%" + (search == null ? "" : search) + "%";
            for (int i = 1; i <= 4; i++) ps.setString(i, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void add(Book b) throws SQLException {
        String sql = """
            INSERT INTO books(title,author,category,isbn,quantity,available_quantity)
            VALUES(?,?,?,?,?,?)
            """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,b.title());
            ps.setString(2,b.author());
            ps.setString(3,b.category());
            ps.setString(4,b.isbn());
            ps.setInt(5,b.quantity());
            ps.setInt(6,b.quantity());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM books WHERE id=?")) {
            ps.setInt(1,id);
            ps.executeUpdate();
        }
    }

    private Book map(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("id"), rs.getString("title"), rs.getString("author"),
            rs.getString("category"), rs.getString("isbn"),
            rs.getInt("quantity"), rs.getInt("available_quantity")
        );
    }
}
