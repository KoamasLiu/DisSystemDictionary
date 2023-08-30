/**
 * Name: Haoyu Liu
 * Student id: 1385415
 */

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.net.ServerSocketFactory;

public class Main {
    private static int port = 6666; // Default port number.

    public static void main(String[] args) throws JSONException, IOException {
        if(args.length < 2) {
            System.out.println("Usage: java -jar DictionaryServer.jar 6666 Dictionary.txt");
            System.exit(1);
        }
        port = Integer.parseInt(args[0]);
        String dictionaryFilePath = args[1];


        // Initialize the shared dictionary.
        DictionaryModify sharedDict = new DictionaryModify(dictionaryFilePath);


        // Create a custom handler for rejected tasks in the thread pool.
        ThreadsPool.RejectedExecutionHandler handler = (task, executor) -> {
            System.out.println("Task rejected: " + task);
        };

        // Initialize a thread pool with a specified number of threads and max tasks.
        ThreadsPool threadPool = new ThreadsPool(10, 15, handler);  // 10 threads, 15 max tasks

        // Get the default server socket factory.
        ServerSocketFactory factory = ServerSocketFactory.getDefault();

        try(ServerSocket server = factory.createServerSocket(port)) {
            System.out.println("Welcome to Haoyu's dictionary program!");
            while(true) {

                // Wait for client connections.
                Socket client = server.accept();

                // For each client connection, serve the client using a separate thread.
                threadPool.execute(() -> serveClient(client,sharedDict));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }finally {

            // Gracefully shutdown the thread pool when finished.
            threadPool.shutdown();
        }
    }

    /**
     * Method to handle a client request.
     * @param client
     * @param dict
     */
    private static void serveClient(Socket client,DictionaryModify dict) {
        try (Socket clientSocket = client) {
            String getMessage;
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            // Read the client's request.
            getMessage = input.readUTF();
            JSONObject jsonObject = new JSONObject(getMessage);
            String key = (String) jsonObject.keys().next();

            // Process the client's request based on the action provided (query, add, remove, update).
            switch (key) {
                case "query":
                    String queryWord = jsonObject.getString("query");
                    String queryAns = dict.query(queryWord);
                    output.writeUTF(queryAns);
                    break;

                case "add":
                    JSONObject addmessage = jsonObject.getJSONObject("add");
                    String addword = addmessage.getString("word");
                    String addmeaning = addmessage.getString("meaning");
                    output.writeUTF(dict.add(addword,addmeaning));
                    break;

                case "remove":
                    String removeWord = jsonObject.getString("remove");
                    output.writeUTF(dict.remove(removeWord));
                    break;

                case "update":
                    JSONObject updatemessage = jsonObject.getJSONObject("update");
                    String updateword = updatemessage.getString("word");
                    String updatemeaning = updatemessage.getString("meaning");
                    output.writeUTF(dict.update(updateword,updatemeaning));
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
