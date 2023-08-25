import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;


import javax.net.ServerSocketFactory;

public class Main {
    private static int port = 6666;
    private static int counter = 0;
    private static DictionaryModify sharedDict;

    static {
        try {
            sharedDict = new DictionaryModify("Dictionary.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws JSONException, IOException, InterruptedException {

        ThreadsPool.RejectedExecutionHandler handler = (task, executor) -> {
            System.out.println("Task rejected: " + task);
        };
        ThreadsPool threadPool = new ThreadsPool(10, 15, handler);  // 10 threads, 15 max tasks
        ServerSocketFactory factory = ServerSocketFactory.getDefault();

        try(ServerSocket server = factory.createServerSocket(port)) {
            System.out.println("Waiting for client connection-");

            while(true) {
                Socket client = server.accept();
                counter++;
                System.out.println("Client "+counter+": Applying for connection!");

                threadPool.execute(() -> serveClient(client,sharedDict));

//                Thread t = new Thread(() -> serveClient(client));
//                t.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }finally {
            threadPool.shutdown();  // shut down the thread pool gracefully
        }


//        try {
//            DictionaryModify dict = new DictionaryModify("Dictionary.txt");
//            System.out.println(dict.query("apple"));   // Outputs: A fruit that is usually red or green.
//            System.out.println(dict.add("banana", "A yellow fruit."));  // Outputs: Success
//            System.out.println(dict.remove("book"));   // Outputs: Success
//            System.out.println(dict.update("car", "A four-wheeled vehicle."));   // Outputs: Success
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }

    }
    private static void serveClient(Socket client,DictionaryModify dict) {
        try (Socket clientSocket = client) {
            String getMessage;

            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            // Output Stream
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            getMessage = input.readUTF();
            JSONObject jsonObject = new JSONObject(getMessage);
            String key = (String) jsonObject.keys().next();

            switch (key) {
                case "query":
                    String queryWord = jsonObject.getString("query");
                    System.out.println(queryWord);
                    String queryAns = dict.query(queryWord);
                    output.writeUTF(queryAns);
                    break;

                case "add":
                    Map<String, String> addWord = new HashMap<>();
                    output.writeUTF(dict.add(addWord.keySet().toString(), addWord.values().toString()));
                    break;

                case "remove":
                    String removeWord = jsonObject.getString("remove");
                    output.writeUTF(dict.remove(removeWord));
                    break;

                case "update":
                    Map<String, String> updateWord = new HashMap<>();
                    output.writeUTF(dict.update(updateWord.keySet().toString(), updateWord.values().toString()));
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
