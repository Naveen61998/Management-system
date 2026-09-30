package com.library;

import com.library.server.LibraryServer;

public class Main {
    public static void main(String[] args) throws Exception {
        LibraryServer server = new LibraryServer(8080);
        server.start();
        System.out.println("Library Management System: http://localhost:8080");
    }
}
