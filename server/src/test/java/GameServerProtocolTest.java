import controller.Run;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class GameServerProtocolTest {
    @Test public void registrationLoginAndInvalidCommandsUseSeparateConnections() throws Exception {
        String[] names={"jsonUsersInfo.txt","jsonUsernames.txt","jsonNicknames.txt"};
        Map<String,byte[]> saved=new HashMap<>();
        for(String name:names) if(Files.exists(Path.of(name))) saved.put(name,Files.readAllBytes(Path.of(name)));
        int port;
        try(ServerSocket temporary=new ServerSocket(0)){port=temporary.getLocalPort();}
        System.setProperty("game.port",Integer.toString(port));
        controller.SetCards.readingCSVFileTrapSpell();
        controller.SetCards.readingCSVFileMonster();
        Thread server=new Thread(Run::run);server.setDaemon(true);server.start();
        try {
            for(int client=0;client<2;client++) {
                Socket socket=null;
                for(int attempt=0;attempt<100;attempt++) {
                    try {socket=new Socket("127.0.0.1",port);break;} catch(IOException e){Thread.sleep(20);}
                }
                assertNotNull(socket);
                try(Socket connection=socket) {
                    connection.setSoTimeout(2000);
                    ObjectOutputStream objects=new ObjectOutputStream(connection.getOutputStream());objects.flush();
                    ObjectInputStream received=new ObjectInputStream(connection.getInputStream());
                    DataOutputStream out=new DataOutputStream(connection.getOutputStream());
                    DataInputStream in=new DataInputStream(connection.getInputStream());
                    String user="test"+UUID.randomUUID().toString().replace("-","");
                    out.writeUTF("R user create --username "+user+" --nickname "+user+" --password test-only --imageURL /images/profile/char0.jpg");out.flush();
                    assertEquals("user created successfully!",in.readUTF());
                    out.writeUTF("R user login --username "+user+" --password test-only");out.flush();
                    String login=in.readUTF();
                    assertTrue(login.startsWith("user logged in successfully!"));
                    String token=login.substring("user logged in successfully!".length());
                    out.writeUTF(token);out.flush();
                    model.UserModel loaded=(model.UserModel)received.readObject();
                    assertEquals(user,loaded.getUsername());
                    out.writeUTF("unrecognized");out.flush();
                    assertEquals("invalid command",in.readUTF());
                }
            }
        } finally {
            for(String name:names) if(saved.containsKey(name)) Files.write(Path.of(name),saved.get(name));else Files.deleteIfExists(Path.of(name));
        }
    }
    @Test public void cardModelsRoundTripThroughObjectSerialization() throws Exception {
        model.MonsterCard card=new model.MonsterCard("EARTH","Demo",3,"Warrior",1200,800,"Monster","Normal",false,"synthetic",100,"Demo");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(card);}
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            model.MonsterCard restored=(model.MonsterCard)in.readObject();
            assertEquals("Demo",restored.getCardName());assertEquals(1200,restored.getAttack());
        }
    }
}
