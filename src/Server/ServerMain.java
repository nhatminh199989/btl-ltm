/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Server.controller.Server;
import Server.controller.ServerData;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Watermelon
 */

public class ServerMain {        
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(10000);
        ServerData sd = new ServerData();
        while(true){
            Socket socket = ss.accept();
            Server server = new Server(sd, socket);
            server.start();
        }        
    }
    
}
