/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.controller;

import Client.controller.Client;
import Client.model.Account;
import Client.model.Message;
import Client.model.Room;
import Client.model.RoomClientSide;
import Server.ServerMain;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Watermelon
 */
public class Server extends Thread {

    private final Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private ServerData sd;
    private Account acc;

    public Server(ServerData sd, Socket clientSocket) throws IOException {
        this.sd = sd;
        this.clientSocket = clientSocket;
        this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
        try {
            handlerClientSocket();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void handlerClientSocket() throws IOException, ClassNotFoundException, SQLException {
        Message message;
        System.out.println("Start server");
        while (true) {
            message = (Message) inputStream.readObject();
            if (message != null) {
                String header = message.getHeader();
                switch (header) {
                    case "LOGIN": {
                        System.out.println("Get client login request");
                        login(message);
                        break;
                    }
                    case "GETROOM": {
                        System.out.println("client get room request");
                        getRoomList();
                        break;
                    }
                    case "JOIN": {
                        System.out.println("Client join room request");
                        joinRoom(message);
                        break;
                    }
                    case "ROOMMESS": {
                        System.out.println("Sending mess to room");
                        sendMessToRoom(message);
                        break;
                    }
                    case "LEAVEROOM": {
                        System.out.println("Leaving room");
                        leaveRoom(message);
                        break;
                    }
                    case "REGISTER": {
                        System.out.println("Register");
                        register(message);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    public void leaveRoom(Message message) throws IOException {
        String RoomName = message.getTo();
        Room room = sd.getRoomByName(RoomName);
        String from = message.getFrom();
        room.sendLeave(from);
        //System.out.println(from+"is leaving room "+room.getName());
    }

    public void register(Message message) throws ClassNotFoundException, SQLException, IOException {
        String username = (String) message.getContent();
        String password = (String) message.getFrom();
        Class.forName("com.mysql.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/baitaplon", "root", "");
        String sql = "INSERT INTO taikhoan(username,pwd) VALUES(?,?) ";
        PreparedStatement prepStmt = con.prepareStatement(sql);
        prepStmt.setString(1, username);
        prepStmt.setString(2, password);
        int check = prepStmt.executeUpdate();
        Message res = new Message("RES_Register",check,"","");
        sendMessage(res);
    }

    public void sendMessToRoom(Message message) throws IOException {
        String RoomName = message.getTo();
        String chat = message.getFrom() + ":" + (String) message.getContent();
        Room room = sd.getRoomByName(RoomName);
        System.out.println(chat);
        room.sendMessage(message.getFrom(), (String) message.getContent());
    }

    public void joinRoom(Message message) throws IOException {
        String RoomName = message.getTo();
        String Username = message.getFrom();
        ArrayList<Room> roomList = this.sd.getList();
        for (Room i : roomList) {
            if (i.getName().equalsIgnoreCase(RoomName)) {
                i.addUser(Username);
                i.addServer(this);
                i.ClientJoinRoom(Username);
                Message m = new Message("JOINROOM", i.ClientsideRoom(), "", "");
                System.out.println(m.toString());
                outputStream.reset();
                outputStream.writeObject(m);
                outputStream.flush();
            }
        }
    }

    public void getRoomList() throws IOException {
        ArrayList<Room> roomList = this.sd.getList();
        ArrayList<RoomClientSide> roomClientSideList = new ArrayList<RoomClientSide>();
        for (Room i : roomList) {
            roomClientSideList.add(new RoomClientSide(i.getName(), i.getChatHistory(), i.getUser()));
        }
        Message res = new Message("ROOMLIST", roomClientSideList, "", "");
        outputStream.writeObject(res);
    }

    public Account getAcc() {
        return this.acc;
    }

    public void sendMessage(Message message) throws IOException {
        outputStream.writeObject(message);
        outputStream.flush();
    }

    public void login(Message message) throws IOException {
        try {
            String username = (String) message.getContent();
            String password = message.getFrom();
            this.acc = new Account(username, password);
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/baitaplon", "root", "");
            String sql = "SELECT * FROM taikhoan WHERE username = ? AND pwd =? ";
            PreparedStatement prepStmt = con.prepareStatement(sql);
            prepStmt.setString(1, username);
            prepStmt.setString(2, password);
            ResultSet kq = prepStmt.executeQuery();
            Message res = new Message();
            if (kq.next() == false) {
                res.setContent("loginFALSE");
            } else {
                res.setContent("loginOK");
            }
            outputStream.writeObject(res);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
