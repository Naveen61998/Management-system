package com.library.model;

public record Book(int id, String title, String author, String category,
                   String isbn, int quantity, int availableQuantity) {}
