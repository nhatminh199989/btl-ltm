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
        Room phong1 = new Room("Phòng 1 - 230.0.0.0");
        Room phong2 = new Room("Phòng 2 - 230.0.0.1");
        Room phong3 = new Room("Phòng 3 - 230.0.0.2");
        this.list.add(phong1);
        this.list.add(phong2);
        this.list.add(phong3);
    }

    public ArrayList<Room> getList() {
        return list;
    }
    
    public Room getRoomByName(String name){
        for(Room i : list){
            if(i.getName().equals(name)){
                return i;
            }
        }
        return null;
    }    

    public void setList(ArrayList<Room> list) {
        this.list = list;
    }
    
        
}
