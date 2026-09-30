package com.library.server;

import com.library.dao.*;
import com.library.model.*;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class LibraryServer {
    private final HttpServer server;
    private final BookDAO books=new BookDAO();
    private final MemberDAO members=new MemberDAO();
    private final IssueDAO issues=new IssueDAO();

    public LibraryServer(int port)throws IOException{
        server=HttpServer.create(new InetSocketAddress(port),0);
        server.createContext("/",this::staticFile);
        server.createContext("/api/books",this::books);
        server.createContext("/api/members",this::members);
        server.createContext("/api/issues",this::issues);
    }
    public void start(){server.start();}

    private void staticFile(HttpExchange e)throws IOException{
        if(!"GET".equalsIgnoreCase(e.getRequestMethod())){send(e,405,"Method Not Allowed","text/plain");return;}
        String p=e.getRequestURI().getPath();
        if("/".equals(p))p="/index.html";
        Path root=Paths.get("src/main/resources/static").toAbsolutePath().normalize();
        Path file=root.resolve(p.substring(1)).normalize();
        if(!file.startsWith(root)||!Files.exists(file)){send(e,404,"Not Found","text/plain");return;}
        String type=p.endsWith(".css")?"text/css":p.endsWith(".js")?"application/javascript":"text/html";
        send(e,200,Files.readString(file),""+type);
    }

    private void books(HttpExchange e)throws IOException{
        try{
            if("GET".equalsIgnoreCase(e.getRequestMethod())){
                String q=query(e,"search");List<Book> a=books.findAll(q);StringBuilder j=new StringBuilder("[");
                for(int i=0;i<a.size();i++){Book b=a.get(i);if(i>0)j.append(",");
                    j.append(String.format(Locale.US,"{\"id\":%d,\"title\":\"%s\",\"author\":\"%s\",\"category\":\"%s\",\"isbn\":\"%s\",\"quantity\":%d,\"availableQuantity\":%d}",
                    b.id(),esc(b.title()),esc(b.author()),esc(b.category()),esc(b.isbn()),b.quantity(),b.availableQuantity()));}
                j.append("]");send(e,200,j.toString(),"application/json");return;
            }
            if("POST".equalsIgnoreCase(e.getRequestMethod())){
                Map<String,String> p=form(read(e));
                books.add(new Book(0,p.get("title"),p.get("author"),p.get("category"),p.get("isbn"),Integer.parseInt(p.get("quantity")),0));
                send(e,201,"{\"message\":\"Book added\"}","application/json");return;
            }
            if("DELETE".equalsIgnoreCase(e.getRequestMethod())){books.delete(Integer.parseInt(query(e,"id")));send(e,200,"{\"message\":\"Book deleted\"}","application/json");return;}
            send(e,405,"Method Not Allowed","text/plain");
        }catch(Exception x){send(e,400,error(x),"application/json");}
    }

    private void members(HttpExchange e)throws IOException{
        try{
            if("GET".equalsIgnoreCase(e.getRequestMethod())){
                List<Member>a=members.findAll(query(e,"search"));StringBuilder j=new StringBuilder("[");
                for(int i=0;i<a.size();i++){Member m=a.get(i);if(i>0)j.append(",");
                    j.append(String.format(Locale.US,"{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"address\":\"%s\"}",
                    m.id(),esc(m.name()),esc(m.email()),esc(m.phone()),esc(m.address())));}
                j.append("]");send(e,200,j.toString(),"application/json");return;
            }
            if("POST".equalsIgnoreCase(e.getRequestMethod())){Map<String,String>p=form(read(e));
                members.add(new Member(0,p.get("name"),p.get("email"),p.get("phone"),p.get("address")));
                send(e,201,"{\"message\":\"Member added\"}","application/json");return;}
            if("DELETE".equalsIgnoreCase(e.getRequestMethod())){members.delete(Integer.parseInt(query(e,"id")));send(e,200,"{\"message\":\"Member deleted\"}","application/json");return;}
            send(e,405,"Method Not Allowed","text/plain");
        }catch(Exception x){send(e,400,error(x),"application/json");}
    }

    private void issues(HttpExchange e)throws IOException{
        try{
            if("GET".equalsIgnoreCase(e.getRequestMethod())){
                List<Issue>a=issues.findAll();StringBuilder j=new StringBuilder("[");
                for(int i=0;i<a.size();i++){Issue x=a.get(i);if(i>0)j.append(",");
                    j.append(String.format(Locale.US,"{\"id\":%d,\"bookId\":%d,\"memberId\":%d,\"bookTitle\":\"%s\",\"memberName\":\"%s\",\"issueDate\":\"%s\",\"dueDate\":\"%s\",\"returnDate\":%s,\"fine\":%s,\"status\":\"%s\"}",
                    x.id(),x.bookId(),x.memberId(),esc(x.bookTitle()),esc(x.memberName()),x.issueDate(),x.dueDate(),
                    x.returnDate()==null?"null":"\""+x.returnDate()+"\"",x.fine(),x.status()));}
                j.append("]");send(e,200,j.toString(),"application/json");return;
            }
            Map<String,String>p=form(read(e));
            if("POST".equalsIgnoreCase(e.getRequestMethod())){issues.issue(Integer.parseInt(p.get("bookId")),Integer.parseInt(p.get("memberId")),LocalDate.parse(p.get("dueDate")));send(e,201,"{\"message\":\"Book issued\"}","application/json");return;}
            if("PUT".equalsIgnoreCase(e.getRequestMethod())){issues.returnBook(Integer.parseInt(p.get("issueId")));send(e,200,"{\"message\":\"Book returned\"}","application/json");return;}
            send(e,405,"Method Not Allowed","text/plain");
        }catch(Exception x){send(e,400,error(x),"application/json");}
    }

    private static String read(HttpExchange e)throws IOException{return new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);}
    private static Map<String,String> form(String b){Map<String,String>m=new HashMap<>();for(String p:b.split("&")){String[]a=p.split("=",2);if(a.length==2)m.put(URLDecoder.decode(a[0],StandardCharsets.UTF_8),URLDecoder.decode(a[1],StandardCharsets.UTF_8));}return m;}
    private static String query(HttpExchange e,String k){String q=e.getRequestURI().getRawQuery();if(q==null)return "";for(String p:q.split("&")){String[]a=p.split("=",2);if(a.length==2&&a[0].equals(k))return URLDecoder.decode(a[1],StandardCharsets.UTF_8);}return "";}
    private static String esc(String s){return s==null?"":s.replace("\\","\\\\").replace("\"","\\\\\"").replace("\n"," ");}
    private static String error(Exception e){return "{\"error\":\""+esc(e.getMessage()==null?e.toString():e.getMessage())+"\"}";}
    private static void send(HttpExchange e,int code,String body,String type)throws IOException{byte[]b=body.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type",type+"; charset=UTF-8");e.sendResponseHeaders(code,b.length);try(OutputStream o=e.getResponseBody()){o.write(b);}}
}
