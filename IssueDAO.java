package com.library.dao;

import com.library.config.Database;
import com.library.model.Issue;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class IssueDAO {
    public List<Issue> findAll() throws SQLException {
        String sql = """
            SELECT i.*, b.title book_title, m.name member_name
            FROM issues i
            JOIN books b ON b.id=i.book_id
            JOIN members m ON m.id=i.member_id
            ORDER BY i.id DESC
            """;
        List<Issue> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date returnDate = rs.getDate("return_date");
                list.add(new Issue(
                    rs.getInt("id"), rs.getInt("book_id"), rs.getInt("member_id"),
                    rs.getString("book_title"), rs.getString("member_name"),
                    rs.getDate("issue_date").toLocalDate(),
                    rs.getDate("due_date").toLocalDate(),
                    returnDate == null ? null : returnDate.toLocalDate(),
                    rs.getBigDecimal("fine"), rs.getString("status")
                ));
            }
        }
        return list;
    }

    public void issue(int bookId, int memberId, LocalDate dueDate) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                    "SELECT available_quantity FROM books WHERE id=? FOR UPDATE")) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt(1) <= 0)
                            throw new SQLException("Book is not available.");
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO issues(book_id,member_id,issue_date,due_date) VALUES(?,?,?,?)")) {
                    ps.setInt(1,bookId);
                    ps.setInt(2,memberId);
                    ps.setDate(3,Date.valueOf(LocalDate.now()));
                    ps.setDate(4,Date.valueOf(dueDate));
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE books SET available_quantity=available_quantity-1 WHERE id=?")) {
                    ps.setInt(1,bookId);
                    ps.executeUpdate();
                }

                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException s) throw s;
                throw new SQLException(e);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void returnBook(int issueId) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int bookId;
                LocalDate due;
                String status;

                try (PreparedStatement ps = c.prepareStatement(
                    "SELECT book_id,due_date,status FROM issues WHERE id=? FOR UPDATE")) {
                    ps.setInt(1,issueId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Issue not found.");
                        bookId = rs.getInt("book_id");
                        due = rs.getDate("due_date").toLocalDate();
                        status = rs.getString("status");
                    }
                }

                if ("RETURNED".equals(status))
                    throw new SQLException("Book already returned.");

                LocalDate today = LocalDate.now();
                long late = Math.max(0, ChronoUnit.DAYS.between(due,today));
                BigDecimal fine = BigDecimal.valueOf(late * 5L);

                try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE issues SET return_date=?,fine=?,status='RETURNED' WHERE id=?")) {
                    ps.setDate(1,Date.valueOf(today));
                    ps.setBigDecimal(2,fine);
                    ps.setInt(3,issueId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE books SET available_quantity=available_quantity+1 WHERE id=?")) {
                    ps.setInt(1,bookId);
                    ps.executeUpdate();
                }

                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException s) throw s;
                throw new SQLException(e);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
