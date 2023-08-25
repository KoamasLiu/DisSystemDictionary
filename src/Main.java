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
                threadPool.execute(() -> serveClient(client,sharedDict));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }finally {
            threadPool.shutdown();  // shut down the thread pool gracefully
        }
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
