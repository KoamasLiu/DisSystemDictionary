import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;


import javax.net.ServerSocketFactory;

public class Main {
    private static int port = 6666;
    private static int counter = 0;
    public static void main(String[] args) {
        ServerSocketFactory factory = ServerSocketFactory.getDefault();

        try(ServerSocket server = factory.createServerSocket(port)) {
            System.out.println("Waiting for client connection-");

            while(true) {
                Socket client = server.accept();
                counter++;
                System.out.println("Client "+counter+": Applying for connection!");

                Thread t = new Thread(() -> serveClient(client));
                t.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        try {
            DictionaryModify dict = new DictionaryModify("Dictionary.txt");
            System.out.println(dict.query("apple"));   // Outputs: A fruit that is usually red or green.
            System.out.println(dict.add("banana", "A yellow fruit."));  // Outputs: Success
            System.out.println(dict.remove("book"));   // Outputs: Success
            System.out.println(dict.update("car", "A four-wheeled vehicle."));   // Outputs: Success
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
    private static void serveClient(Socket client) {
        try(Socket clientSocket = client) {





            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            // Output Stream
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            System.out.println("CLIENT: "+input.readUTF());

            output.writeUTF("Server: Hi Client "+counter+" !!!");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


}
