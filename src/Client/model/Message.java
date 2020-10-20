/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.model;

import java.io.Serializable;

/**
 *
 * @author Watermelon
 */
public class Message<T> implements Serializable{
    private String header;
    private T content;
    private String from;
    private String to;

    public Message(String header, T content, String from, String to) {
        this.header = header;
        this.content = content;
        this.from = from;
        this.to = to;
    }

    public Message() {
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
    
    
}
