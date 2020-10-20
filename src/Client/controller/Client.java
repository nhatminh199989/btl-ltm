/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.controller;

import Client.model.Message;
import Client.model.Room;
import Client.view.Login;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
    private final int serverPort = 8188;
    private Socket socket;
    private ObjectOutputStream serverOut;
    private ObjectInputStream serverIn;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Client(){}

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

    public void getRoom() throws IOException, ClassNotFoundException {
        Message msg = new Message("GETROOM", "", "", "");
        serverOut.writeObject(msg);
        System.out.println("Getting room");
        Message res = (Message) serverIn.readObject();
        ArrayList<Room> roomList = (ArrayList<Room>) res.getContent();
        for(Room i : roomList){
            System.out.println(i.getName());
        }
    }
}
