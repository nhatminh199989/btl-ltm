/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.controller;

import Client.model.Message;
import Client.model.Room;
import Client.model.RoomClientSide;
import Client.view.Login;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.util.ArrayList;
import javax.naming.spi.DirStateFactory.Result;

/**
 *
 * @author Watermelon
 */
public class Client {

    private String username;
    private final String serverName = "localhost";
    private final int serverPort = 10000;
    private MulticastSocket ms;
    private InetAddress group;  
    
    private Socket socket;
    private ObjectOutputStream serverOut;
    private ObjectInputStream serverIn;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Client() {
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            serverOut = new ObjectOutputStream(socket.getOutputStream());
            serverIn = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (IOException ex) {
            System.out.println("Connection error");
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean login(String username, String password) throws IOException, ClassNotFoundException {
        Message msg = new Message("LOGIN", username, password, "");
        System.out.println("Gui message" + msg.getContent());
        serverOut.writeObject(msg);
        Message res = (Message) serverIn.readObject();
        System.out.println("Nhan message" + res.getContent());
        if ("loginOK".equalsIgnoreCase((String) res.getContent())) {
            this.setUsername(username);
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<RoomClientSide> getRoom() throws IOException, ClassNotFoundException {
        Message msg = new Message("GETROOM", "", "", "");
        serverOut.writeObject(msg);
        System.out.println("Getting room");
        Message res = (Message) serverIn.readObject();
        ArrayList<RoomClientSide> roomList = (ArrayList<RoomClientSide>) res.getContent();
        return roomList;
    }

    
    public RoomClientSide joinRoom(String room, String username) throws IOException, ClassNotFoundException {
        Message msg = new Message("JOIN", "", username, room);
        serverOut.writeObject(msg);
        System.out.println("Joinning room");
        Message res = (Message) serverIn.readObject();
        RoomClientSide r = (RoomClientSide) res.getContent();
        ms = new MulticastSocket(4321);
        String udpIP = r.getName().trim().split(" - ")[1].trim();
        group = InetAddress.getByName(udpIP);
        ms.joinGroup(group);
        return r;
    }
    
    public void sendMessToRoom(String mess,String from,String to) throws IOException{
        Message msg = new Message("ROOMMESS",mess,from,to);
        serverOut.writeObject(msg);
        serverOut.flush();
    }   
    
    public void sendLeaveMess(String mess,String from,String to) throws IOException{
        Message msg = new Message("LEAVEROOM",mess,from,to);
        serverOut.writeObject(msg);
        serverOut.flush();
    }
    
    public Message ReadMessLoop() throws IOException, ClassNotFoundException{
        Message m = (Message) serverIn.readObject();
        return m;
    }
    
    public String testGetMess() throws IOException, ClassNotFoundException{
        Message m = (Message) serverIn.readObject();
        String message  = m.getFrom()+":"+(String) m.getContent();
        System.out.println("From client testGetMess");
        return message;
    }
    
    public void sendVoice(byte[] buffer) throws IOException{
        DatagramPacket dgp = new DatagramPacket(buffer,buffer.length, group,4321);
        ms.send(dgp);
    }
        
    public byte[] receiveVoice() throws IOException{
        byte[] buffer = new byte[49152];
        DatagramPacket dgp = new DatagramPacket(buffer,buffer.length);
        ms.receive(dgp);
        return dgp.getData();
    }
}
