package com.library;

import com.library.server.LibraryServer;

public class Main {
    public static void main(String[] args) throws Exception {
        LibraryServer server = new LibraryServer(8080);
        server.start();
        String url = "http://localhost:8080";
        System.out.println();
        System.out.println("==============================================");
        System.out.println(" Library Management System is running");
        System.out.println(" Open: " + url);
        System.out.println(" Press Ctrl+C to stop");
        System.out.println("==============================================");
        System.out.println();

        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
            }
        } catch (Exception ignored) {
            // Browser auto-open is optional.
        }
    }
}
