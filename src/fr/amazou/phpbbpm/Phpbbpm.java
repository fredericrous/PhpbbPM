/*
 * phpbbpm main
 */
package fr.amazou.phpbbpm;

import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author fredericrousseau
 */
public class Phpbbpm extends JavaPlugin {
    private static Logger log;
    private static Config config;
    private static Server server;
    
    @Override
    public void onEnable() { 
        log = Logger.getLogger("Minecraft");
        log.info("[phpbbpm] Enabled");
        
        //load properties
        config = new Config();
        config.load();
        
        server = this.getServer();
        
    BroadCastUnread unread_msg = new BroadCastUnread();
    unread_msg.Start();
    }
    
    @Override
    public void onDisable() { 
        log.info("[phpbbpm] Disabled");
    }
    
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 
        String myCmd = cmd.getName().toLowerCase();
        Commands c = new Commands((Player)sender);
        boolean ret = true;
        
        if (myCmd.equals("pm")) {
            c.Help();
        } else if (myCmd.equals("pmread")) {
            int n = 0;
            if (args.length > 0) {
                try {
                n = Integer.parseInt(args[0]);
                } catch(Exception ex) {
                    c.Close();
                    return false;
                }
              ret = c.Read(n);   
            }
            else
            {
                ret = c.Read(n);
            }
        } else if (myCmd.equals("pmsend")) {
            if (args.length >= 3) {
                ret = c.Send(args[0], args[1], args[2]);
            }
        } else if (myCmd.equals("pmlist")) {
            ret = c.List();
        }
        c.Close();
        return ret;
    }
    
    public static Server getBukkitServer() {
        return server;
    }
    
    public static Config getPluginConfig() {
        return config;
    }
    
    public static Logger getLog() {
        return log;
    }
}