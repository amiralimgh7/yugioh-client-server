package controller;

import model.UserModel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Run {
    private static final ThreadLocal<DataInputStream> dataInputs = new ThreadLocal<>();
    private static final ThreadLocal<DataOutputStream> dataOutputs = new ThreadLocal<>();
    private static final ThreadLocal<ObjectOutputStream> objectOutputs = new ThreadLocal<>();
    private static final ThreadLocal<ObjectInputStream> objectInputs = new ThreadLocal<>();

    public static void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(Integer.getInteger("game.port", 1227), 50, java.net.InetAddress.getLoopbackAddress());
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        dataInputs.set(new DataInputStream(socket.getInputStream()));
                        dataOutputs.set(new DataOutputStream(socket.getOutputStream()));
                        objectOutputs.set(new ObjectOutputStream(socket.getOutputStream()));
                        objectOutputs.get().flush();
                        objectInputs.set(new ObjectInputStream(socket.getInputStream()));
                        while (true) {
                            String input = dataInputs.get().readUTF();
                            // Commands may contain passwords; do not log them.
                            String output = null;
                            if (RegisterAndLoginController.allOnlineUsers.containsKey(input)) {
                                getUserByToken(input);
                                continue;
                            }
                            Pattern pattern = Pattern.compile("^1010(.+?)$");
                            Matcher matcher = pattern.matcher(input);
                            if (matcher.find()) {
                                System.out.println("find" + matcher.group(1));
                                getAllUserDeck(matcher.group(1));
                                continue;
                            }
                            if (input.equals("1020315")) {
                                updateBazar();
                                continue;
                            }
                            if (input.startsWith("R")) {
                                output = RegisterAndLoginController.run(input);

                            }
                            if (input.startsWith("D")) {
                                output = DeckController.run(input);
                            }
                            if (input.startsWith("M")) {
                                output = MainMenuController.findMatcher(input);
                            }
                            if (input.startsWith("S")) {
                                output = ShopController.run(input);
                            }
                            if (input.startsWith("profile")) {
                                output = MainMenuController.profile(input);
                            }
                            if (input.startsWith("BB")) {
                                output = BazarController.findMatcher(input);
                            }
                            if (output == null) output = "invalid command";
                            if (output.equals("continue")) {
                                continue;
                            }
                            // Responses can contain authentication tokens.
                            dataOutputs.get().writeUTF(output);

                            dataOutputs.get().flush();
                        }

                    } catch (EOFException ignored) {
                        // Normal client disconnect.
                    } catch (IOException e) {
                        System.err.println("Connection ended: " + e.getMessage());
                    } finally {
                        dataInputs.remove(); dataOutputs.remove(); objectInputs.remove(); objectOutputs.remove();
                        try { socket.close(); } catch (IOException ignored) {}
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getUserByToken(String token) throws IOException {

        UserModel userModel = UserModel.getUserByUsername(RegisterAndLoginController.allOnlineUsers.get(token));
        objectOutputs.get().writeObject(userModel);
        objectOutputs.get().flush();

    }

    public static void getAllUserDeck(String token) throws IOException {

        UserModel userModel = UserModel.getUserByUsername(RegisterAndLoginController.allOnlineUsers.get(token));
        objectOutputs.get().writeObject(userModel.userAllDecks);
        objectOutputs.get().flush();
    }

    public static void updateBazar() {
        try {
            objectOutputs.get().writeUnshared(UserModel.all);
            objectOutputs.get().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
