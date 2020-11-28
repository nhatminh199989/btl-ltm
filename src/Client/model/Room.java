/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import Server.controller.Server;
import java.io.IOException;

/**
 *
 * @author Watermelon
 */
public class Room implements Serializable {

    private String name;
    private List<String> ChatHistory;
    private List<String> User;
    private ArrayList<Server> ServerList;

    public Room(String name, List<String> ChatHistory, List<String> User) {
        this.name = name;
        this.ChatHistory = ChatHistory;
        this.User = User;
    }

    public RoomClientSide ClientsideRoom() {
        return new RoomClientSide(name, ChatHistory, User);
    }

    public void addServer(Server s) {
        this.ServerList.add(s);
    }

    public Room(String name) {
        this.name = name;
        this.User = new ArrayList<>();
        this.ChatHistory = new ArrayList<>();
        this.ServerList = new ArrayList<Server>();
    }

    public void addUser(String name) {
        this.User.add(name);
    }

    public List<String> getChatHistory() {
        return ChatHistory;
    }

    public void setChatHistory(ArrayList<String> ChatHistory) {
        this.ChatHistory = ChatHistory;
    }

    public List<String> getUser() {
        return User;
    }

    public void setUser(List<String> User) {
        this.User = User;
    }

    public Room(String name, ArrayList<String> ChatHistory, ArrayList<String> User) {
        this.name = name;
        this.ChatHistory = ChatHistory;
        this.User = User;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Room{" + "name=" + name + ", ChatHistory=" + ChatHistory + ", User=" + User + '}';
    }

    public void sendMessage(String from, String msg) throws IOException {
        String message = from + ": " + msg;
        this.ChatHistory.add(message);
        for (Server i : ServerList) {
            if (!i.getAcc().getUsername().equals(from)) {
                Message m = new Message("ROOMMESS", message, from, name);
                i.sendMessage(m);
            }
        }
    }

    public void sendLeave(String from) throws IOException {
        User.remove(from);
        Server LeaveServer = null;
        for (Server i : ServerList) {
            if (i.getAcc().getUsername().equals(from)) {
                LeaveServer = i;
            }
        }
        ServerList.remove(LeaveServer);
        for (Server i : ServerList) {
            if (!i.getAcc().getUsername().equals(from)) {
                Message m = new Message("LEAVE", "", from, name);
                i.sendMessage(m);
                System.out.println(from + " is leaving " + name);
            }

        }
    }    

    public void ClientJoinRoom(String username) throws IOException {
        for (Server i : ServerList) {
            if (!i.getAcc().getUsername().equals(username)) {
                Message m = new Message("CLIENTJOINROOM", username, "", "");
                i.sendMessage(m);
            }
        }
    }

}
