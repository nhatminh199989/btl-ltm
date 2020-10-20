/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.controller;

import Client.controller.Client;
import Client.model.Message;
import Client.model.Room;
import Server.ServerMain;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Watermelon
 */
public class Server extends Thread{
    
    private final Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private ServerData sd;    
    public Server(ServerData sd,Socket clientSocket) throws IOException {
        this.sd = sd;
        this.clientSocket = clientSocket;
        this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
    }
    
    @Override
    public void run(){
        try {
            handlerClientSocket();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void handlerClientSocket() throws IOException, ClassNotFoundException{
        Message message;
        System.out.println("Start server");
        while(true){
            message = (Message) inputStream.readObject();
            if(message != null ){
                String header = message.getHeader();
                switch(header){
                    case "LOGIN":{
                        System.out.println("Get client login request");
                        login(message);
                        break;
                    }
                    case "GETROOM":{
                        System.out.println("client get room request");
                        getRoomList();
                        break;
                    }
                }
            }
        }
    }
    
    public void getRoomList() throws IOException{
        ArrayList<Room> roomList = this.sd.getList();
        Message res = new Message("ROOMLIST",roomList,"","");
        outputStream.writeObject(res);
    }
    
    public void login(Message message) throws IOException{
        try {
            String username = (String) message.getContent();
            String password = message.getFrom();
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/baitaplon","root","");
            String sql = "SELECT * FROM taikhoan WHERE username = ? AND pwd =? ";
            PreparedStatement prepStmt = con.prepareStatement(sql);
            prepStmt.setString(1,username);
            prepStmt.setString(2,password);
            ResultSet kq = prepStmt.executeQuery();
            Message res = new Message();
            if(kq.next() == false){
                res.setContent("loginFALSE");
            }else{
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
