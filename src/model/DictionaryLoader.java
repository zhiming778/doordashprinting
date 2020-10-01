package model;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DictionaryLoader {

    private final String BOM = "\uFEFF";

    public Map<String, String> load(String path) {
        Map<String, String> dictionary = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String line, key = null, value;
            for (int i = 0; (line = br.readLine()) != null; i++) {
                if (i % 2 == 0) {
                    if (i == 0) {
                        line = line.replace(BOM, "");
                    }
                    key = line;
                } else {
                    value = line;
                    dictionary.put(key, value);
                }
            }
            br.close();
            isr.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    public static void main(String[] args) {
        DictionaryLoader loader = new DictionaryLoader();
        Map<String, String> dishes = loader.load("dishes.txt");
        for (Entry<String, String> entry : dishes.entrySet()) {
            // System.out.println(entry.getKey());
        }
        System.out.println(dishes.get("12. Egg Roll or Vegetable Roll"));
    }
}
