import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DictionaryModify {
    private final String filePath;
    private final Map<String, String> entries;

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

    public String query(String word) {
        return entries.get(word);
    }

    public String add(String word, String definition) throws IOException {
        if (entries.containsKey(word)) {
            return "Duplicate";
        }
        entries.put(word, definition);
        saveToFile();
        return "Success";
    }

    public String remove(String word) throws IOException {
        if (entries.remove(word) != null) {
            saveToFile();
            return "Success";
        }
        return "Not found";
    }

    public String update(String word, String definition) throws IOException {
        if (!entries.containsKey(word)) {
            return "Not found";
        }
        entries.put(word, definition);
        saveToFile();
        return "Success";
    }

    private void saveToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        }
    }
}
