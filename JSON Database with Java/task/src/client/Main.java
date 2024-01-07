package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.beust.jcommander.JCommander;

public class Main {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;


    public static void main(String[] args) {

        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream())
        ) {
            //Scanner scanner = new Scanner(System.in);
            System.out.println("Client started!");

            Args arguments = new Args();

            JCommander.newBuilder()
                    .addObject(arguments)
                    .build()
                    .parse(args);


            //String msg = buildMessageForServer(arguments.requestType, arguments.cellIndex, arguments.valueToSave);
            Request requestToServer = new Request(arguments.requestType, arguments.cellIndex, arguments.valueToSave);
            //requestToServer.printRequestJson();

            String msg = requestToServer.getRequestJson();

            output.writeUTF(msg); // send a message to the server
            System.out.println("Sent: " + msg);
            String receivedMsg = input.readUTF(); // read the reply from the server

            System.out.println("Received: " + receivedMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String buildMessageForServer(String requestType, String cellIndex, String valueToSave) {
        String result;

        switch (requestType) {
            case "exit"   : {result = "exit"; break;}
            case "delete" : {result = "delete " + cellIndex; break;}
            case "get"    : {result = "get " + cellIndex; break;}
            case "set"    : {result = "set " + cellIndex + " " + valueToSave; break;}
            default       : {System.out.println("Wrong command"); result = "no command";}
        }

        return result;
    }
}
