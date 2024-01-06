package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.net.*;

public class Main {

    private static final String[] DB_ARRAY = new String[1000];
    private static final Scanner scanner = new Scanner(System.in);

    private static String serverAnswer = "";


    public static void main(String[] args) {

        String address = "127.0.0.1";
        int port = 23456;

        Arrays.fill(DB_ARRAY, "");

        try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address))) {

            System.out.println("Server started!");

            boolean isExit = false;
            String[] userCommand;
            String commandType;


            while (!isExit) {
                try (
                        Socket socket = server.accept(); // accept a new client
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {
                    String msg = input.readUTF(); // read a message from the client
                    System.out.println("Received: " + msg);

                    userCommand = acceptUserCommand(msg);
                    commandType = userCommand[0];

                    switch (commandType) {
                        case "exit"   : {isExit = true; setServerAnswer("OK"); break;}
                        case "delete" : {deleteOperation(userCommand); break;}
                        case "get"    : {getOperation(userCommand); break;}
                        case "set"    : {setOperation(userCommand); break;}
                        default       : System.out.println("No such command");
                    }

                    /* The part from the 2nd stage
                    String[] msgWords = msg.split(" ");
                    String recordNumber = msgWords[msgWords.length - 1];
                    String answer = "A record # " + recordNumber + " was sent!";
                    output.writeUTF(answer); // resend it to the client
                    System.out.println("Sent: " + answer);
                    */


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

    private static void setServerAnswer(String answer) {
        serverAnswer = answer;
    }

    private static String getUserString(String[] userCommand) {
        StringBuilder userString = new StringBuilder();
        for (int i = 2; i < userCommand.length; i++) {
            userString.append(userCommand[i]).append(" ");
        }
        return userString.toString().trim();
    }

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

    private static String[] acceptUserCommand(String msg) {
        return msg.split(" ");
    }
}
