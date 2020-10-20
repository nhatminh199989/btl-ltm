/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.controller;

import Client.model.Room;
import java.util.ArrayList;

/**
 *
 * @author Watermelon
 */
public class ServerData {
    private ArrayList<Room> list = new ArrayList<Room>();
    
    public ServerData() {        
        Room phong1 = new Room("Phòng 1");
        Room phong2 = new Room("Phòng 2");
        Room phong3 = new Room("Phòng 3");
        this.list.add(phong1);
        this.list.add(phong2);
        this.list.add(phong3);
    }

    public ArrayList<Room> getList() {
        return list;
    }

    public void setList(ArrayList<Room> list) {
        this.list = list;
    }
    
        
}
