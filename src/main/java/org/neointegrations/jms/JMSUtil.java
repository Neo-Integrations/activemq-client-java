package org.neointegrations.jms;

import java.io.*;
import java.util.Properties;

public class JMSUtil {
    public static Properties prop = null;
    static {
        try (InputStream input = new FileInputStream("config.properties")) {
            prop = new Properties();
            prop.load(input);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static String getProperties(String property) {
        if(prop == null) return null;
        return prop.getProperty(property);
    }
}
