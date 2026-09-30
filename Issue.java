package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Issue(int id, int bookId, int memberId, String bookTitle,
                    String memberName, LocalDate issueDate, LocalDate dueDate,
                    LocalDate returnDate, BigDecimal fine, String status) {}
