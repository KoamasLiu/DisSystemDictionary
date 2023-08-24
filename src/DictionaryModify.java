import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DictionaryModify {
    private final String filePath;
    private final Map<String, String> entries;
    private boolean Dictionary_Occupancy = false;

    public DictionaryModify(String filePath) throws IOException {
        this.filePath = filePath;
        this.entries = new HashMap<>();
        loadFromFile();
    }

    private void loadFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    entries.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
    }

    synchronized private void saveToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        }
    }

    public String query(String word) throws InterruptedException {
        while (Dictionary_Occupancy){
            wait();
        }
        return entries.get(word);
    }

    synchronized public String add(String word, String definition) throws IOException, InterruptedException {
        while (Dictionary_Occupancy){
            wait();
        }
        if (entries.containsKey(word)) {
            return "Duplicate";
        }
        entries.put(word, definition);
        saveToFile();
        Dictionary_Occupancy = false;
        notifyAll();
        return "Success";
    }

    synchronized public String remove(String word) throws IOException, InterruptedException {
        while (Dictionary_Occupancy){
            wait();
        }
        if (entries.remove(word) != null) {
            saveToFile();
            return "Success";
        }
        Dictionary_Occupancy = false;
        notifyAll();
        return "Not found";
    }

    synchronized public String update(String word, String definition) throws IOException, InterruptedException {
        while (Dictionary_Occupancy){
            wait();
        }
        if (!entries.containsKey(word)) {
            return "Not found";
        }
        entries.put(word, definition);
        saveToFile();
        Dictionary_Occupancy = false;
        notifyAll();
        return "Success";
    }
}
