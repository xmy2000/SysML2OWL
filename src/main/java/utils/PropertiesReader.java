package utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
    public static String getProperty(String key) throws IOException {
        Properties properties = new Properties();
        InputStream in = new BufferedInputStream(new FileInputStream("src/main/resources/config.properties"));
        properties.load(in);
        return properties.getProperty(key);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(getProperty("SysML_path"));
        System.out.println(getProperty("base_owl_path"));
        System.out.println(getProperty("owl_source_url"));
        System.out.println(getProperty("owl_out_path"));
    }
}
