/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Watermelon
 */
public class Room implements Serializable{
    private String name;
    private ArrayList<String> ChatHistory;
    private ArrayList<String> User;

    public Room(String name) {
        this.name = name;
        this.User = new ArrayList<>();
        this.ChatHistory = new ArrayList<>();
    }   

    public void addUser(String name){
        this.User.add(name);
    }
    
    public ArrayList<String> getChatHistory() {
        return ChatHistory;
    }

    public void setChatHistory(ArrayList<String> ChatHistory) {
        this.ChatHistory = ChatHistory;
    }

    public ArrayList<String> getUser() {
        return User;
    }

    public void setUser(ArrayList<String> User) {
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
    
}
