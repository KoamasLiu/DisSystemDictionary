import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DictionaryModify {
    private final String filePath; // Path to the dictionary file.
    private final Map<String, String> entries; // In-memory representation of the dictionary.
    private boolean Dictionary_Occupancy = false; // Flag to indicate if the dictionary is currently being modified.

    /**
     * Constructor that initializes the dictionary and loads the entries from the file.
     * @param filePath
     * @throws IOException
     * @throws JSONException
     */
    public DictionaryModify(String filePath) throws IOException, JSONException {
        this.filePath = filePath;
        this.entries = new HashMap<>();
        loadFromFile();
    }

    /**
     * Loads the dictionary entries from the file into the in-memory map.
     * @throws IOException
     * @throws JSONException
     */
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

    /**
     * Saves the in-memory map of dictionary entries back to the file.
     * @throws IOException
     * @throws JSONException
     */
    synchronized private void saveToFile() throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject(entries);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(jsonObject.toString(4)); // 4 spaces for indentation
        }
    }

    /**
     * Queries the dictionary for the given word and returns its definition.
     * @param word
     * @return
     * @throws InterruptedException
     */
    synchronized public String query(String word) throws InterruptedException {
        while (Dictionary_Occupancy){
            wait();
        }
        String Message = entries.get(word);
        if (Message == null){
            return "Word not found";
        }else{
            return Message;
        }
    }

    /**
     * Adds a word and its definition to the dictionary.
     * @param word
     * @param definition
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws JSONException
     */
    synchronized public String add(String word, String definition)
            throws IOException, InterruptedException, JSONException {
        while (Dictionary_Occupancy){
            wait();
        }
        Dictionary_Occupancy = true;
        if (entries.containsKey(word)) {
            Dictionary_Occupancy = false;
            notifyAll();
            return "The words are already in the dictionary";
        }
        entries.put(word, definition);
        saveToFile();
        Dictionary_Occupancy = false;
        notifyAll();
        return "The word has been successfully added to the dictionary";
    }

    /**
     * Removes a word from the dictionary.
     * @param word
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws JSONException
     */
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
            return "The word has been successfully removed from the dictionary";
        }
        Dictionary_Occupancy = false;
        notifyAll();
        return "This word is not found in the dictionary";
    }

    /**
     * Updates the definition of a word in the dictionary.
     * @param word
     * @param definition
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws JSONException
     */
    synchronized public String update(String word, String definition)
            throws IOException, InterruptedException, JSONException {
        while (Dictionary_Occupancy){
            wait();
        }
        Dictionary_Occupancy = true;
        if (!entries.containsKey(word)) {
            Dictionary_Occupancy = false;
            notifyAll();
            return "This word is not found in the dictionary";
        }
        entries.put(word, definition);
        saveToFile();
        Dictionary_Occupancy = false;
        notifyAll();
        return "Dictionary has been updated";
    }
}
