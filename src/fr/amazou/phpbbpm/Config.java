/*
 * Config class for phpbbpm
 */
package fr.amazou.phpbbpm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * make/load config file
 * 
 * @author Zougi
 */
public class Config {

    private final static String       pluginDirPath = "plugins/phpbbpm";
    private final static File         pseudoFile    = new File(pluginDirPath + "/pseudo.fix");
    private final static File         configFile    = new File(pluginDirPath + "/config.properties");
    private Logger                    log;
    private Properties                prop;

    private String                    db_url;
    private String                    db_name;
    private String                    db_user;
    private String                    db_pass;
    private String                    db_prefix;

    private String                    unread_warning;
    private Integer                   unread_warning_delay;

    private Integer                   sign_update_delay;
    private String                    sign_detection_string;
    private String                    sign_msg;

    private List<Map<String, String>> pseudoList;

    public Config() {
        log = Logger.getLogger("Minecraft");

        unread_warning = "%s pm unread.";
        unread_warning_delay = 7;

        sign_update_delay = 5;
        sign_detection_string = "Mail";
        sign_msg = "%s unread msg";

        checkPluginDirExists();

        prop = new Properties();
        if (!configFile.exists()) {
            makeConfigFile();
        }
        if (!pseudoFile.exists()) {
            makePseudoFile();
        }
    }

    /**
     * check is the plugin directory exists and make it if it does not
     */
    private void checkPluginDirExists() {
        File pluginDir = new File(pluginDirPath);
        if (!pluginDir.isDirectory()) {
            pluginDir.mkdir();
        }
    }

    /**
     * make a default config file
     */
    private void makeConfigFile() {
        try {
            configFile.createNewFile();
            FileWriter writer = new FileWriter(configFile);
            prop.store(writer, "PhpbbPM configuration file");
            writer.write("\n#Phpbb config\n");
            prop_put(writer, "db_prefix", "phpbb_table_prefix");
            prop_put(writer, "db_pass", "mysql_password");
            prop_put(writer, "db_user", "mysql_login");
            prop_put(writer, "db_name", "mysql_phpbb_database");
            prop_put(writer, "db_url", "jdbc:mysql://localhost:3306/");

            writer.write("\n#PhpbbPM config\n");
            prop_put(writer, "unread_warning", unread_warning);
            prop_put(writer, "unread_warning_delay", unread_warning_delay.toString());
            prop_put(writer, "sign_update_delay", sign_update_delay.toString());
            prop_put(writer, "sign_detection_string", sign_detection_string);
            prop_put(writer, "sign_msg", sign_msg);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            log.info("[phpbbpm] Could not create " + configFile.getPath());
        }
    }

    /**
     * load config
     */
    public void load() {
        try {
            FileReader reader = new FileReader(configFile);
            prop.load(reader);
            db_url = prop.getProperty("db_url");
            db_name = prop.getProperty("db_name");
            db_user = prop.getProperty("db_user");
            db_pass = prop.getProperty("db_pass");
            db_prefix = prop.getProperty("db_prefix");
            unread_warning = prop.getProperty("unread_warning");
            try {
                unread_warning_delay = Integer.parseInt(prop.getProperty("unread_warning_delay"));
                sign_update_delay = Integer.parseInt(prop.getProperty("sign_update_delay"));
            } catch (NumberFormatException ex) {
                log.info(String.format("[phpbbpm] Could not load unread_warning_delay" + " or sign_update_delay propertie," + " unread_warning_delay set to %d min, sign_update_delay = %d", unread_warning_delay, sign_update_delay));
            }
            sign_detection_string = prop.getProperty("sign_detection_string");
            sign_msg = prop.getProperty("sign_msg");
            reader.close();
        } catch (IOException ex) {
            log.info("[phpbbpm] Could not load " + configFile.getPath());
        }
    }

    /**
     * doesnt want to use store because it doesnt order keys and config file was a mess
     * 
     * @param writer
     *            streamwriter
     * @param key
     *            config key
     * @param val
     *            config key value
     * @throws IOException
     */
    private void prop_put(FileWriter writer, String key, String val) throws IOException {
        writer.write(String.format("%s=%s\n", key, val));
    }

    /**
     * create the pseudo.fix file
     */
    private void makePseudoFile() {
        try {
            pseudoFile.createNewFile();
            FileWriter writer = new FileWriter(pseudoFile);
            writer.write("# ingame_pseudo=forum_pseudo");
            writer.close();
        } catch (IOException ex) {
            log.log(Level.WARNING, "[PhpbbPM] Can't create " + pseudoFile);
        }
    }

    /**
     * load pseudo.fix file
     */
    public void loadPseudoList() {
        pseudoList = new ArrayList<Map<String, String>>();
        try {
            FileReader fileStream = new FileReader(pseudoFile);
            BufferedReader reader = new BufferedReader(fileStream);
            String str;
            int separator_pos;
            Map<String, String> map;
            while ((str = reader.readLine()) != null) {
                if (!str.startsWith("#")) {
                    separator_pos = str.lastIndexOf('=');
                    map = new HashMap<String, String>();
                    map.put("ingame", str.substring(0, separator_pos));
                    map.put("forum", str.substring(separator_pos + 1, str.length()));
                    pseudoList.add(map);
                }
            }
            reader.close();
            fileStream.close();
        } catch (FileNotFoundException ex) {
            log.log(Level.INFO, "[PhpbbPM] Can't find " + pseudoFile);
        } catch (IOException e) {
            log.log(Level.WARNING, "[PhpbbPM] Can't read " + pseudoFile);
        } catch (Exception ef) {
            log.info(ef.getMessage());
        }
    }

    /**
     * @return cnx string
     */
    public String getDB_url() {
        return db_url;
    }

    /**
     * @return database name
     */
    public String getDB_name() {
        return db_name;
    }

    /**
     * @return database user
     */
    public String getDB_user() {
        return db_user;
    }

    /**
     * @return database password
     */
    public String getDB_pass() {
        return db_pass;
    }

    /**
     * @return database table prefix
     */
    public String getDB_prefix() {
        return db_prefix;
    }

    /**
     * @return display broadcast msg
     */
    public String getWarnMsg() {
        return unread_warning;
    }

    /**
     * @return delay to schedule the reminder
     */
    public int getWarnDelay() {
        return unread_warning_delay;
    }

    /**
     * @return delay to schedule the sign updater
     */
    public int getSignDelay() {
        return sign_update_delay;
    }

    /**
     * @return display msg on sign
     */
    public String getSignDetectionString() {
        return sign_detection_string;
    }

    /**
     * @return display msg on sign
     */
    public String getSignMsg() {
        return sign_msg;
    }

    public List<Map<String, String>> getPseudoList() {
        return pseudoList;
    }
}
