package model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class JUtils {
    public static void print(String text) {
        long millis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm]");
        Calendar calendar = Calendar.getInstance();
        
        System.out.println(sdf.format(calendar.getTime())+text);
    }
}
