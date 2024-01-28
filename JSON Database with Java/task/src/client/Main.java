package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

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

            JCommander.newBuilder()
                    .addObject(arguments)
                    .build()
                    .parse(args);


            if (Objects.isNull(arguments.requestFile) || arguments.requestFile.isEmpty()) {
                requestToServer = new Request(arguments.requestType, arguments.cellIndex, arguments.valueToSave);
            } else {

                try {
                    String requestFileContent = new String(Files.readAllBytes(Paths.get(PATH_TO_REQUEST_FILES.concat(arguments.requestFile))));
                    //System.out.println(requestFileContent);
                    HashMap<String, String> clientRequest = new Gson().fromJson(requestFileContent, HashMap.class);
                    if ("set".equals(clientRequest.get("type"))) {
                        requestToServer = new Request(clientRequest.get("type"), clientRequest.get("key"), clientRequest.get("value"));
                    } else if ("exit".equals(clientRequest.get("type"))) {
                        requestToServer = new Request(clientRequest.get("type"));
                    } else {
                        requestToServer = new Request(clientRequest.get("type"), clientRequest.get("key"));
                    }
                } catch (IOException e) {
                    System.out.println("Cannot read file: " + e.getMessage());
                }
            }

            //System.out.println("Request got: ");
            //requestToServer.printRequestJson();

            String msg = requestToServer.getRequestJson();

            output.writeUTF(msg); // send a message to the server
            System.out.println("Sent: " + msg);
            String receivedMsg = input.readUTF(); // read the reply from the server

            System.out.println("Received: " + receivedMsg);

        } catch (IOException e) {
            e.printStackTrace();
        }


        /*
                                THE TESTING BLOCK:
            sends multiple requests to server of different types (but not "exit")
        */

        //if (!requestToServer.isExit())

        if (false)

        {

            Random randomType = new Random();
            Random randomKey = new Random();
            Random randomValue = new Random();
            String requestType;
            String requestKey;
            String requestValue;

            for (int i = 1; i < 11; i++) {

                try (
                        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {

                    System.out.println("Client started!");

                    requestKey = "key" + i;
                    requestValue = "value" + i;
                    requestToServer = new Request("set", requestKey, requestValue);

                    String msg = requestToServer.getRequestJson();

                    output.writeUTF(msg); // send a message to the server
                    System.out.println("Sent: " + msg);
                    String receivedMsg = input.readUTF(); // read the reply from the server

                    System.out.println("Received: " + receivedMsg);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            for (int i = 1; i < 101; i++) {

                try (
                        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {

                    System.out.println("Client started!");

                    if (i % 5 != 0) {
                        requestType = "get";
                    } else {
                        requestType = randomType.nextBoolean() ? "set" : "delete";
                    }

                    requestKey = "key" + randomKey.nextInt(11);
                    requestValue = "value" + randomValue.nextInt(7);


                    if ("set".equals(requestType)) {
                        requestToServer = new Request(requestType, requestKey, requestValue);
                    } else {
                        requestToServer = new Request(requestType, requestKey);
                    }


                    String msg = requestToServer.getRequestJson();

                    output.writeUTF(msg); // send a message to the server
                    System.out.println("Sent: " + msg);
                    String receivedMsg = input.readUTF(); // read the reply from the server

                    System.out.println("Received: " + receivedMsg);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        ////////////////////END OF THE TESTING BLOCK///////////////////////


    }

}
