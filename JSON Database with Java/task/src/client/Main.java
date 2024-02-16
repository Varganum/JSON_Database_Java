package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

public class Main {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;

    private static final String PATH_TO_REQUEST_FILES = "C:\\Java\\JSON Database (Java)\\JSON Database (Java)\\task\\src\\client\\data\\";


    public static void main(String[] args) {

        Args arguments = new Args();
        Request requestToServer = null;

        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream())
        ) {

            System.out.println("Client started!");

            //Parse command line parameters
            JCommander.newBuilder()
                    .addObject(arguments)
                    .build()
                    .parse(args);


            /* Check if there isn't "-in" parameter in command line, or it is empty. If one of this is true read
            other parameters from command line and create new Request object. Otherwise, read all parameters for
            request from the .json file.
            */

            if (Objects.isNull(arguments.requestFile) || arguments.requestFile.isEmpty()) {
                requestToServer = new Request(arguments.requestType, arguments.cellIndex, arguments.valueToSave);
            } else {

                try {
                    //read the content of the .json file to a String
                    String requestFileContent = new String(Files.readAllBytes(Paths.get(PATH_TO_REQUEST_FILES.concat(arguments.requestFile))));

                    //parse the content of .json file to a HashMap
                    HashMap<String, String> clientRequest = new Gson().fromJson(requestFileContent, HashMap.class);

                    //create the Request depending on its type
                    if ("set".equals(clientRequest.get("type"))) {
                        requestToServer = new Request(clientRequest.get("type"), clientRequest.get("key"), clientRequest.get("value"));
                    } else if ("exit".equals(clientRequest.get("type"))) {
                        requestToServer = new Request(clientRequest.get("type"));
                    } else {
                        //for Request types - "get" and "delete"
                        requestToServer = new Request(clientRequest.get("type"), clientRequest.get("key"));
                    }
                } catch (IOException e) {
                    System.out.println("Cannot read file: " + e.getMessage());
                }
            }


            //convert the Request object to a String
            String msg = requestToServer.getRequestJson();

            //send converted Request to the Server
            output.writeUTF(msg); // send a message to the server
            System.out.println("Sent: " + msg);

            //get an answer from the Server
            String receivedMsg = input.readUTF(); // read the reply from the server

            System.out.println("Received: " + receivedMsg);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
