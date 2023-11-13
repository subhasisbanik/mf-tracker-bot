package org.bot.utils;

import org.bot.Bot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {

    Properties prop = new Properties();
    public PropertyUtils(){

        try (InputStream input = Bot.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return;
            }
            //load a properties file from class path, inside static method
            prop.load(input);
            System.out.println(prop.getProperty("telegram.bot.username"));
            System.out.println(prop.getProperty("telegram.bot.token"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getPropertyValue(String propertyName){
        return prop.getProperty(propertyName);
    }
}
