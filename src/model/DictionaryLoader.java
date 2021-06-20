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

    private final char BOM = '\uFEFF';

    public Map<String, String> load(String path) {
        if(path == null)
            throw new NullPointerException("path can't be null.");
        Map<String, String> dictionary = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String line, key = null, value;
            for (int i = 0; (line = br.readLine()) != null; i++) {
                if (i % 2 == 0) {
                    if (i == 0 && line.charAt(0) == BOM)
                        key = line.substring(1);
                    else
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
        Map<String, String> dishes = loader.load("a");
        for (Entry<String, String> entry : dishes.entrySet()) {
             System.out.println(entry.getKey());
        }
    }
}
