import java.io.IOException;
import java.util.Dictionary;

public class Main {
    public static void main(String[] args) {
        try {
            DictionaryModify dict = new DictionaryModify("Dictionary.txt");
            System.out.println(dict.query("apple"));   // Outputs: A fruit that is usually red or green.
            System.out.println(dict.add("banana", "A yellow fruit."));  // Outputs: Success
            System.out.println(dict.remove("book"));   // Outputs: Success
            System.out.println(dict.update("car", "A four-wheeled vehicle."));   // Outputs: Success
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
