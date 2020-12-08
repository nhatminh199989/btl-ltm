/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.view;

import Client.controller.Client;
import Client.model.Message;
import Client.model.Room;
import Client.model.RoomClientSide;
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import java.awt.Color;
import java.awt.PopupMenu;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author Watermelon
 */
public class ChatScreen extends javax.swing.JFrame {

    /**
     * Creates new form ChatScreen
     */
    private RoomClientSide room;
    private Client client;
    private DefaultListModel dlm = new DefaultListModel();
    ReadDataThread rdt = new ReadDataThread();
    private JDialog roomPickerDialog;
    private boolean is_recording = false;
    private boolean is_playing = false;
    public static ByteArrayOutputStream out;
    public static record r;
    public static playSound ps;
    public byte[] audioData;
    ReadVoice rv = new ReadVoice();

    public ChatScreen(RoomClientSide room, Client client, JDialog dialog) throws IOException, ClassNotFoundException {
        initComponents();
        this.jButton2.setText("►");
        this.jButton3.setText("►");
        this.roomPickerDialog = dialog;
        this.jTextPane1.setEditable(false);
        this.room = room;
        this.client = client;
        String udpIP = room.getName().trim().split(" - ")[1].trim();
        this.jLabel1.setText(udpIP);

        //Setup user list
        jList2.setModel(dlm);
        for (String i : room.getUser()) {
            dlm.addElement(i);
        }
        this.jLabel2.setText("danh sách: " + room.getUser().size());
        //Setup chat
        String chat = "";
        for (String j : room.getChatHistory()) {
            String name = j.split(":")[0];
            if (name == client.getUsername()) {
                chat = chat + "bạn :" + j.split(":")[2] + "\n";
            } else {
                chat = chat + j + "\n";
            }
        }
        jTextPane1.setText(chat);
        rdt.start();
        rv.start();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                try {
                    client.sendLeaveMess("",client.getUsername(),room.getName());
                } catch (IOException ex) {
                    Logger.getLogger(ChatScreen.class.getName()).log(Level.SEVERE, null, ex);
                }
                previousDialog();                
            }
        });
    }

    
    private void previousDialog() {
        this.setVisible(false);
        this.roomPickerDialog.setVisible(true);
        this.dispose();
        System.exit(0);
    }

    private ChatScreen() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void inputMess(String msg,Color color) throws BadLocationException{
        SimpleAttributeSet sas = new SimpleAttributeSet(jTextPane1.getCharacterAttributes());
        StyleConstants.setForeground(sas, color);
        jTextPane1.getDocument().insertString(jTextPane1.getDocument().getLength(), msg, sas);
    }
    
    class ReadVoice extends Thread {

        public void run() {
            while (true) {
                try {
                    audioData = client.receiveVoice();
                    inputMess("Bạn nhận được một voice chat", Color.red);
                    System.out.println("Đã nhận được voice");
                } catch (IOException ex) {
                    Logger.getLogger(ChatScreen.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BadLocationException ex) {
                    Logger.getLogger(ChatScreen.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }
    }

    class ReadDataThread extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    Message m = client.ReadMessLoop();
                    String header = m.getHeader();
                    switch (header) {
                        case "ROOMMESS": {
                            String a = (String) m.getContent();
                            inputMess(a +"\n", Color.black);
                            break;
                        }
                        case "CLIENTJOINROOM": {
                            String a = (String) m.getContent();
                            String chatText = jTextPane1.getText();
                            dlm.addElement(a);
                            jLabel2.setText("danh sách: " + dlm.size());                            
                            inputMess(a+" đã vào phòng \n", Color.red);
                            break;
                        }
                        case "LEAVE":{
                            String user = (String) m.getFrom();
                            for(int i = 0; i< dlm.getSize();i++){
                                if(dlm.get(i).equals(user)){
                                    dlm.remove(i);
                                }
                            }  
                            String leaveRoom = user+ " đã rời khỏi phòng \n";
                            inputMess(leaveRoom, Color.red);
                            jLabel2.setText("danh sách: " + dlm.size());
                        }
                        default: {
                            break;
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ChatScreen.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ChatScreen.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BadLocationException ex) {
                    Logger.getLogger(ChatScreen.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    class playSound extends Thread {

        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
        AudioInputStream audioInputStream;
        SourceDataLine sourceDataLine;

        public void run() {
            try {
                InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
                audioInputStream = new AudioInputStream(byteArrayInputStream, format, audioData.length / format.getFrameSize());
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                sourceDataLine.open(format);
                sourceDataLine.start();
                int cnt = 0;
                byte tempBuffer[] = new byte[10000];
                while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    }
                }
                sourceDataLine.drain();
                sourceDataLine.close();
                jLabel4.setText("SPEAKER");
                jButton3.setText("►");
                is_playing = false;
            } catch (LineUnavailableException ex) {
                System.out.println(ex);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    class record extends Thread {

        public void run() {
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            TargetDataLine microphone;
            try {
                out = new ByteArrayOutputStream();
                System.out.println("Recording");
                microphone = AudioSystem.getTargetDataLine(format);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                int numBytesRead;
                int CHUNK_SIZE = 1024;
                byte[] data = new byte[microphone.getBufferSize() / 5];
                microphone.start();
                int bytesRead = 0;
                while (is_recording == true && bytesRead <= 49152) {
                    numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                    bytesRead = bytesRead + numBytesRead;
                    System.out.println(bytesRead);
                    out.write(data, 0, numBytesRead);
                }
                microphone.close();
                is_recording = false;
                jButton2.setText("►");
                jLabel3.setText("MIC");
                r = null;
                try {
                    client.sendVoice(out.toByteArray());
                } catch (IOException ex) {
                    Logger.getLogger(ChatScreen.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("closed");
            } catch (LineUnavailableException ex) {
                System.out.println(ex);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(jTextPane1);

        jTextField1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
        });

        jButton1.setText("SEND");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("jLabel1");
        jLabel1.setName(""); // NOI18N

        jScrollPane3.setViewportView(jList2);

        jLabel2.setText("jLabel2");

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel3.setText("MIC");

        jLabel4.setText("SPEAKER");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jScrollPane1))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 279, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(186, 186, 186))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField1)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // TODO add your handling code here:
        //get text 
        if (this.jTextField1.getText() != null) {
            String clientChat = this.jTextField1.getText();
            String ChatPanel = this.jTextPane1.getText();
            ChatPanel = ChatPanel + "bạn: " + clientChat + "\n";
            this.jTextPane1.setText(ChatPanel); 
            try {
                this.client.sendMessToRoom(clientChat, this.client.getUsername(), this.room.getName());
            } catch (IOException ex) {
                Logger.getLogger(ChatScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.jTextField1.setText("");
        }
    }//GEN-LAST:event_jButton1MouseClicked

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER && this.jTextField1.getText() != null) {
            //get text 
            String clientChat = this.jTextField1.getText();
            String ChatPanel = this.jTextPane1.getText();
            ChatPanel = ChatPanel + "bạn: " + clientChat + "\n";
            this.jTextPane1.setText(ChatPanel);
            try {
                this.client.sendMessToRoom(clientChat, this.client.getUsername(), this.room.getName());
            } catch (IOException ex) {
                Logger.getLogger(ChatScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
            jTextField1.setText("");
        }
    }//GEN-LAST:event_jTextField1KeyPressed

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        if (is_recording == false) {
            is_recording = true;
            this.jButton2.setText("||");
            this.jLabel3.setText("Recording");
            r = new record();
            r.start();

        } else {
            is_recording = false;
            this.jButton2.setText("►");
            this.jLabel3.setText("MIC");
            r = null;
            try {
                client.sendVoice(out.toByteArray());
            } catch (IOException ex) {
                Logger.getLogger(ChatScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton2MouseClicked


    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        // TODO add your handling code here:
        if (is_playing == false) {
            is_playing = true;
            this.jButton3.setText("||");
            this.jLabel4.setText("Playing");
            ps = new playSound();
            ps.start();
        } else {
            is_playing = false;
            this.jButton3.setText("►");
            this.jLabel4.setText("SPEAKER");
            ps.stop();
        }
    }//GEN-LAST:event_jButton3MouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ChatScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChatScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChatScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChatScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChatScreen().setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JList jList2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
