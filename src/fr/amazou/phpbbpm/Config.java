/*
 * Config class for phpbbpm
 */
package fr.amazou.phpbbpm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 * @author fredericrousseau
 */
public class Config {
   
   private static String pluginDirPath = "plugins/phpbbpm";
   private static File configFile = new File(pluginDirPath +"/config.properties");
   private Logger log;
   private Properties prop;
   private String db_url;
   private String db_name;
   private String db_user;
   private String db_pass;
   private String db_prefix;
   
    public Config() {
        log = Logger.getLogger("Minecraft");
        checkPluginDirExists();
 
        prop = new Properties();       
        if (!configFile.exists()) {
            makeConfigFile();
        }
    }

    private void checkPluginDirExists() {
        File pluginDir = new File(pluginDirPath);
        if (!pluginDir.isDirectory()) {
            pluginDir.mkdir();
        }
    }

    private void makeConfigFile() {
        try {
            configFile.createNewFile();
            FileOutputStream out = new FileOutputStream(configFile);
            prop.put("db_url", "jdbc:mysql://localhost:3306/");
            prop.put("db_name", "mysql_phpbb_database");
            prop.put("db_user", "mysql_login");
            prop.put("db_pass", "mysql_password");
            prop.put("db_prefix", "phpbb_table_prefix");
            prop.store(out, "phpbb config for phpbbpm");
            out.flush();
            out.close();          
            } catch (IOException ex) {
                log.info("[phpbbpm] Could not create " + configFile.getPath());
            }
    }

    public void load() {
        try {
            FileInputStream in = new FileInputStream(configFile);
            prop.load(in);
            db_url = prop.getProperty("db_url");
            db_name = prop.getProperty("db_name");
            db_user = prop.getProperty("db_user");
            db_pass = prop.getProperty("db_pass");
            db_prefix = prop.getProperty("db_prefix");
            in.close();    
        } catch (IOException ex) {
            log.info("[phpbbpm] Could not load " + configFile.getPath());
        }
    }
    
    public String getDB_url() {
        return db_url;
    }
    
    public String getDB_name() {
        return db_name;
    }
    
    public String getDB_user() {
        return db_user;
    }
    
    public String getDB_pass() {
        return db_pass;
    }
    
    public String getDB_prefix() {
        return db_prefix;
    }

}
