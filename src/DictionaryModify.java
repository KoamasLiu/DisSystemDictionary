import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DictionaryModify {
    private final String filePath;
    private final Map<String, String> entries;
    private boolean Dictionary_Occupancy = false;

    public DictionaryModify(String filePath) throws IOException, JSONException {
        this.filePath = filePath;
        this.entries = new HashMap<>();
        loadFromFile();
    }

    private void loadFromFile() throws IOException, JSONException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        JSONObject jsonObject = new JSONObject(content.toString());
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            entries.put(key, jsonObject.getString(key));
        }
    }

    synchronized private void saveToFile() throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject(entries);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(jsonObject.toString(4)); // 4 spaces for indentation
        }
    }

    synchronized public String query(String word) throws InterruptedException {
        while (Dictionary_Occupancy){
            wait();
        }
        return entries.get(word);
    }

    synchronized public String add(String word, String definition)
            throws IOException, InterruptedException, JSONException {
        while (Dictionary_Occupancy){
            wait();
        }
        Dictionary_Occupancy = true;
        if (entries.containsKey(word)) {
            Dictionary_Occupancy = false;
            notifyAll();
            return "Duplicate";
        }
        entries.put(word, definition);
        saveToFile();
        Dictionary_Occupancy = false;
        notifyAll();
        return "Success";
    }

    synchronized public String remove(String word)
            throws IOException, InterruptedException, JSONException {
        while (Dictionary_Occupancy){
            wait();
        }
        Dictionary_Occupancy = true;
        if (entries.remove(word) != null) {
            saveToFile();
            Dictionary_Occupancy = false;
            notifyAll();
            return "Success";
        }
        Dictionary_Occupancy = false;
        notifyAll();
        return "Not found";
    }

    synchronized public String update(String word, String definition)
            throws IOException, InterruptedException, JSONException {
        while (Dictionary_Occupancy){
            wait();
        }
        Dictionary_Occupancy = true;
        if (!entries.containsKey(word)) {
            Dictionary_Occupancy = false;
            notifyAll();
            return "Not found";
        }
        entries.put(word, definition);
        saveToFile();
        Dictionary_Occupancy = false;
        notifyAll();
        return "Success";
    }
}
