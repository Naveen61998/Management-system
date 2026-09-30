package com.library.server;

import com.library.dao.BookDAO;
import com.library.dao.IssueDAO;
import com.library.dao.MemberDAO;
import com.library.model.Book;
import com.library.model.Issue;
import com.library.model.Member;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class LibraryServer {
    private final HttpServer server;
    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final IssueDAO issueDAO = new IssueDAO();

    public LibraryServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::staticFiles);
        server.createContext("/api/books", this::books);
        server.createContext("/api/members", this::members);
        server.createContext("/api/issues", this::issues);
    }

    public void start() { server.start(); }

    private void books(HttpExchange ex) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                String q = query(ex, "search");
                sendJson(ex, 200, booksJson(bookDAO.findAll(q)));
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String,String> f = form(readBody(ex));
                bookDAO.add(new Book(0, req(f,"title"), req(f,"author"),
                    f.getOrDefault("category",""), f.getOrDefault("isbn",""),
                    Integer.parseInt(req(f,"quantity")), 0));
                sendJson(ex, 201, "{\"message\":\"Book added\"}");
            } else if ("DELETE".equalsIgnoreCase(ex.getRequestMethod())) {
                bookDAO.delete(Integer.parseInt(query(ex,"id")));
                sendJson(ex,200,"{\"message\":\"Book deleted\"}");
            } else methodNotAllowed(ex);
        } catch (Exception e) { error(ex,e); }
    }

    private void members(HttpExchange ex) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex,200,membersJson(memberDAO.findAll(query(ex,"search"))));
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String,String> f=form(readBody(ex));
                memberDAO.add(new Member(0,req(f,"name"),f.getOrDefault("email",""),
                    f.getOrDefault("phone",""),f.getOrDefault("address","")));
                sendJson(ex,201,"{\"message\":\"Member added\"}");
            } else if ("DELETE".equalsIgnoreCase(ex.getRequestMethod())) {
                memberDAO.delete(Integer.parseInt(query(ex,"id")));
                sendJson(ex,200,"{\"message\":\"Member deleted\"}");
            } else methodNotAllowed(ex);
        } catch(Exception e) { error(ex,e); }
    }

    private void issues(HttpExchange ex) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex,200,issuesJson(issueDAO.findAll()));
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String,String> f=form(readBody(ex));
                issueDAO.issue(Integer.parseInt(req(f,"bookId")),
                    Integer.parseInt(req(f,"memberId")),LocalDate.parse(req(f,"dueDate")));
                sendJson(ex,201,"{\"message\":\"Book issued\"}");
            } else if ("PUT".equalsIgnoreCase(ex.getRequestMethod())) {
                issueDAO.returnBook(Integer.parseInt(query(ex,"id")));
                sendJson(ex,200,"{\"message\":\"Book returned\"}");
            } else methodNotAllowed(ex);
        } catch(Exception e) { error(ex,e); }
    }

    private void staticFiles(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if ("/".equals(path)) path="/index.html";

        String resource = "src/main/resources/static" + path;
        Path file = Paths.get(resource).normalize();

        if (!file.startsWith(Paths.get("src/main/resources/static").normalize()) ||
            !Files.exists(file) || Files.isDirectory(file)) {
            sendText(ex,404,"Not Found","text/plain");
            return;
        }

        String type = contentType(file.toString());
        byte[] data=Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type",type);
        ex.sendResponseHeaders(200,data.length);
        try(OutputStream out=ex.getResponseBody()){out.write(data);}
    }

    private static String contentType(String p) {
        if(p.endsWith(".html")) return "text/html; charset=UTF-8";
        if(p.endsWith(".css")) return "text/css; charset=UTF-8";
        if(p.endsWith(".js")) return "application/javascript; charset=UTF-8";
        return "application/octet-stream";
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try(InputStream in=ex.getRequestBody()) {
            return new String(in.readAllBytes(),StandardCharsets.UTF_8);
        }
    }

    private static Map<String,String> form(String body) throws UnsupportedEncodingException {
        Map<String,String> map=new HashMap<>();
        if(body==null || body.isBlank()) return map;
        for(String part:body.split("&")) {
            String[] a=part.split("=",2);
            String k=URLDecoder.decode(a[0],StandardCharsets.UTF_8);
            String v=a.length>1?URLDecoder.decode(a[1],StandardCharsets.UTF_8):"";
            map.put(k,v);
        }
        return map;
    }

    private static String query(HttpExchange ex,String key) {
        String q=ex.getRequestURI().getRawQuery();
        if(q==null) return "";
        try {
            for(String p:q.split("&")) {
                String[] a=p.split("=",2);
                if(URLDecoder.decode(a[0],StandardCharsets.UTF_8).equals(key))
                    return a.length>1?URLDecoder.decode(a[1],StandardCharsets.UTF_8):"";
            }
        } catch(Exception ignored){}
        return "";
    }

    private static String req(Map<String,String> f,String key) {
        String v=f.get(key);
        if(v==null || v.isBlank()) throw new IllegalArgumentException(key+" is required");
        return v.trim();
    }

    private static String esc(String s) {
        if(s==null)return "";
        return s.replace("\\","\\\\").replace("\"","\\\"")
            .replace("\r","\\r").replace("\n","\\n");
    }

    private static String booksJson(List<Book> list) {
        StringBuilder b=new StringBuilder("[");
        for(int i=0;i<list.size();i++) {
            Book x=list.get(i);
            if(i>0)b.append(",");
            b.append("{\"id\":").append(x.id())
             .append(",\"title\":\"").append(esc(x.title()))
             .append("\",\"author\":\"").append(esc(x.author()))
             .append("\",\"category\":\"").append(esc(x.category()))
             .append("\",\"isbn\":\"").append(esc(x.isbn()))
             .append("\",\"quantity\":").append(x.quantity())
             .append(",\"availableQuantity\":").append(x.availableQuantity()).append("}");
        }
        return b.append("]").toString();
    }

    private static String membersJson(List<Member> list) {
        StringBuilder b=new StringBuilder("[");
        for(int i=0;i<list.size();i++) {
            Member x=list.get(i);
            if(i>0)b.append(",");
            b.append("{\"id\":").append(x.id())
             .append(",\"name\":\"").append(esc(x.name()))
             .append("\",\"email\":\"").append(esc(x.email()))
             .append("\",\"phone\":\"").append(esc(x.phone()))
             .append("\",\"address\":\"").append(esc(x.address())).append("\"}");
        }
        return b.append("]").toString();
    }

    private static String issuesJson(List<Issue> list) {
        StringBuilder b=new StringBuilder("[");
        for(int i=0;i<list.size();i++) {
            Issue x=list.get(i);
            if(i>0)b.append(",");
            b.append("{\"id\":").append(x.id())
             .append(",\"bookId\":").append(x.bookId())
             .append(",\"memberId\":").append(x.memberId())
             .append(",\"bookTitle\":\"").append(esc(x.bookTitle()))
             .append("\",\"memberName\":\"").append(esc(x.memberName()))
             .append("\",\"issueDate\":\"").append(x.issueDate())
             .append("\",\"dueDate\":\"").append(x.dueDate())
             .append("\",\"returnDate\":").append(x.returnDate()==null?"null":"\""+x.returnDate()+"\"")
             .append(",\"fine\":").append(x.fine()==null?"0":x.fine())
             .append(",\"status\":\"").append(esc(x.status())).append("\"}");
        }
        return b.append("]").toString();
    }

    private static void sendJson(HttpExchange ex,int code,String body) throws IOException {
        sendText(ex,code,body,"application/json; charset=UTF-8");
    }

    private static void sendText(HttpExchange ex,int code,String body,String type) throws IOException {
        byte[] data=body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type",type);
        ex.sendResponseHeaders(code,data.length);
        try(OutputStream out=ex.getResponseBody()){out.write(data);}
    }

    private static void methodNotAllowed(HttpExchange ex) throws IOException {
        sendJson(ex,405,"{\"error\":\"Method not allowed\"}");
    }

    private static void error(HttpExchange ex,Exception e) throws IOException {
        int code=e instanceof IllegalArgumentException?400:500;
        sendJson(ex,code,"{\"error\":\""+esc(e.getMessage()==null?"Server error":e.getMessage())+"\"}");
    }
}
