package cs555RS.util;

import cs555RS.nodes.Client;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InteractiveCommandParser
        implements Runnable {

    private Client client;

    public InteractiveCommandParser(Client node) {
        this.client = node;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String command = sc.nextLine();
            try {
                executeCommand(command);
            } catch (UnknownHostException ex) {
                Logger.getLogger(InteractiveCommandParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(InteractiveCommandParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(InteractiveCommandParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void executeCommand(String command) throws UnknownHostException, IOException, Exception {
        if ((command.startsWith("store")) || (command.startsWith("STORE")) || (command.startsWith("Store"))) {
            StringTokenizer tokenizedCommand = new StringTokenizer(command);
            StringBuffer buf = new StringBuffer();
            for (int i = 0; tokenizedCommand.hasMoreTokens(); i++) {
                if (i == 0) {
                    tokenizedCommand.nextToken();
                }
                buf.append(tokenizedCommand.nextToken());
                buf.append(" ");
            }
            String temp = buf.toString();
            String fileName = temp.trim();
            FileSplit fs = new FileSplit(new File(fileName));
            this.client.storeFile(fs);
        }
        if ((command.startsWith("read")) || (command.startsWith("READ")) || (command.startsWith("Read"))) {
            StringTokenizer tokenizedCommand = new StringTokenizer(command);
            StringBuffer buf = new StringBuffer();
            for (int i = 0; tokenizedCommand.hasMoreTokens(); i++) {
                if (i == 0) {
                    tokenizedCommand.nextToken();
                }
                buf.append(tokenizedCommand.nextToken());
                buf.append(" ");
            }
            String temp = buf.toString();
            String fileName = temp.trim();
            this.client.sendReadRequest(fileName);
        }
    }
}
