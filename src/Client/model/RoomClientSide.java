/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.model;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Watermelon
 */
public class RoomClientSide implements Serializable{
    private String name;
    private List<String> ChatHistory;
    private List<String> User;

    public RoomClientSide(String name, List<String> ChatHistory, List<String> User) {
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

    public List<String> getChatHistory() {
        return ChatHistory;
    }

    public void setChatHistory(List<String> ChatHistory) {
        this.ChatHistory = ChatHistory;
    }

    public List<String> getUser() {
        return User;
    }

    public void setUser(List<String> User) {
        this.User = User;
    }
    
}
