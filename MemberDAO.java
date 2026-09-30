package com.library.dao;

import com.library.config.Database;
import com.library.model.Member;
import java.sql.*;
import java.util.*;

public class MemberDAO {
    public List<Member> findAll(String search) throws SQLException {
        String sql = """
            SELECT * FROM members
            WHERE name LIKE ? OR email LIKE ? OR phone LIKE ?
            ORDER BY id DESC
            """;
        List<Member> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String q = "%" + (search == null ? "" : search) + "%";
            ps.setString(1,q); ps.setString(2,q); ps.setString(3,q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Member(
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("phone"),
                        rs.getString("address")
                    ));
                }
            }
        }
        return list;
    }

    public void add(Member m) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO members(name,email,phone,address) VALUES(?,?,?,?)")) {
            ps.setString(1,m.name());
            ps.setString(2,m.email());
            ps.setString(3,m.phone());
            ps.setString(4,m.address());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM members WHERE id=?")) {
            ps.setInt(1,id);
            ps.executeUpdate();
        }
    }
}
