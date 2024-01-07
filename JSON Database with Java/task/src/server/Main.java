package server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.net.*;

public class Main {

    /* Before 4th stage method implementation
        private static final String[] DB_ARRAY = new String[1000];
    */

    private static final Map<String, String> DB_MAP = new HashMap<>();

    private static final Map<String, String> answerToClient = new LinkedHashMap<>();

    private static String serverAnswer = "";


    public static void main(String[] args) {

        String address = "127.0.0.1";
        int port = 23456;


        try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address))) {

            System.out.println("Server started!");

            /* Before 4th stage method implementation
            String[] userCommand;
            */

            boolean isExit = false;
            String commandType;
            HashMap<String, String> clientRequest;


            while (!isExit) {
                try (
                        Socket socket = server.accept(); // accept a new client
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {
                    String msg = input.readUTF(); // read a message from the client
                    System.out.println("Received: " + msg);

                    /* Before 4th stage method implementation
                    userCommand = acceptUserCommand(msg);
                    */

                    clientRequest = new Gson().fromJson(msg, HashMap.class);
                    commandType = clientRequest.get("type");

                    switch (commandType) {
                        case "exit"   : {isExit = true; updateAnswerToClient("OK"); break;}
                        case "delete" : {deleteOperation(clientRequest); break;}
                        case "get"    : {getOperation(clientRequest); break;}
                        case "set"    : {setOperation(clientRequest); break;}
                        default       : System.out.println("No such command");
                    }

                    /* The part from the 2nd stage
                    String[] msgWords = msg.split(" ");
                    String recordNumber = msgWords[msgWords.length - 1];
                    String answer = "A record # " + recordNumber + " was sent!";
                    output.writeUTF(answer); // resend it to the client
                    System.out.println("Sent: " + answer);
                    */

                    /* The part up to 4th stage

                    output.writeUTF(serverAnswer); // send the answer to the client
                    System.out.println("Sent: " + serverAnswer);
                    setServerAnswer("");
                    */

                    serverAnswer = new Gson().toJson(answerToClient);
                    output.writeUTF(serverAnswer); // send the answer to the client
                    System.out.println("Sent: " + serverAnswer);
                    setServerAnswer("");

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Stage 1 part

        do {
        } while (!isExit);
        */

        //System.out.println(Arrays.toString(DB_ARRAY));


    }

    private static void setOperation(HashMap<String, String> request) {

        DB_MAP.put(request.get("key"), request.get("value"));
        updateAnswerToClient("OK");

    }

    private static void updateAnswerToClient(String response) {
        answerToClient.clear();
        answerToClient.put("response", response);
    }

    private static void updateAnswerToClientWithReason(String response, String reason) {
        updateAnswerToClient(response);
        answerToClient.put("reason", reason);
    }

    private static void updateAnswerToClientWithValue(String response, String value) {
        updateAnswerToClient(response);
        answerToClient.put("value", value);
    }

    private static void setServerAnswer(String answer) {
        serverAnswer = answer;
    }


    private static void getOperation(HashMap<String, String> request) {

        if (DB_MAP.containsKey(request.get("key"))) {
            if ("".equals(DB_MAP.get(request.get("key")))) {
                updateAnswerToClientWithReason("ERROR", "No such key");
                //System.out.println("ERROR");
            } else {
                //System.out.println(DB_ARRAY[index - 1]);
                updateAnswerToClientWithValue("OK", DB_MAP.get(request.get("key")));
            }
        } else {
            updateAnswerToClientWithReason("ERROR", "No such key");
        }
    }

    private static void deleteOperation(HashMap<String, String> request) {

        if (DB_MAP.containsKey(request.get("key"))) {
            DB_MAP.remove(request.get("key"));
            updateAnswerToClient("OK");
        } else {
            updateAnswerToClientWithReason("ERROR", "No such key");
        }
    }


    /* Before 4th stage method implementation

    private static String getUserString(String[] userCommand) {
        StringBuilder userString = new StringBuilder();
        for (int i = 2; i < userCommand.length; i++) {
            userString.append(userCommand[i]).append(" ");
        }
        return userString.toString().trim();
    }
    */

    /* Before 4th stage method implementation

    private static void setOperation(String[] userCommand) {
        int index = getCellIndex(userCommand);
        if (index != - 1) {
            if (userCommand.length > 2) {
                DB_ARRAY[index - 1] = getUserString(userCommand);
            } else {
                DB_ARRAY[index - 1] = "";
            }
            setServerAnswer("OK");
            //System.out.println("OK");
        } else {
            setServerAnswer("ERROR");
            //System.out.println("ERROR");
        }

    }
    */

    /* Before 4th stage method implementation

    private static int getCellIndex(String[] userCommand) {
        int result = - 1;
        if (userCommand.length == 1) {
            System.out.println("Wrong command format.");
        } else {
            result = Integer.parseInt(userCommand[1]);
            if (result > DB_ARRAY.length || result < 1) {
                System.out.println("ERROR");
                result = - 1;
            }
        }
        return result;
    }
    */

    /* Before 4th stage method implementation

    private static void getOperation(String[] userCommand) {
        int index = getCellIndex(userCommand);
        if (index != - 1) {
            if ("".equals(DB_ARRAY[index - 1])) {
                setServerAnswer("ERROR");
                //System.out.println("ERROR");
            } else {
                //System.out.println(DB_ARRAY[index - 1]);
                setServerAnswer(DB_ARRAY[index - 1]);
            }
        }
    }
    */

    /* Before 4th stage method implementation

    private static void deleteOperation(String[] userCommand) {
        int index = getCellIndex(userCommand);
        if (index != - 1) {
            if (!"".equals(DB_ARRAY[index - 1])) {
                DB_ARRAY[index - 1] = "";
            }
            //System.out.println("OK");
            setServerAnswer("OK");
        } else {
            setServerAnswer("ERROR");
        }
    }
    */

    /* Before 4th stage method implementation

    private static String[] acceptUserCommand(String msg) {
        return msg.split(" ");
    }
    */
}
