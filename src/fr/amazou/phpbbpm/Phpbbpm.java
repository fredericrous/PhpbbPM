/*
 * Phpbbpm plugin
 */
package fr.amazou.phpbbpm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Phpbbpm main class
 * 
 * @author Zougi
 */
public class Phpbbpm extends JavaPlugin {
    private static Logger log;
    private static Config config;
    private static Server server;

    @Override
    public void onEnable() {
        log = Logger.getLogger("Minecraft");
        log.info("[phpbbpm] Enabled");

        config = new Config();
        config.loadPseudoList(); // load pseudo.fix
        config.load(); // load config.properties

        server = this.getServer();

        SqlManager sql = new SqlManager();
        sql.CreateSignTable();
        sql.Close();

        PluginManager manager = server.getPluginManager();
        manager.registerEvents(new PhpbbpmListener(), this);

        BroadCastUnread unread_msg = new BroadCastUnread();
        unread_msg.StartReminder();
        unread_msg.StartSignUpdater();
    }

    @Override
    public void onDisable() {
        server.getScheduler().cancelTasks(this);
        log.info("[phpbbpm] Disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        String myCmd = cmd.getName().toLowerCase();
        Commands c = new Commands((Player) sender);
        boolean ret = true;

        if (myCmd.equals("pmhelp")) {
            c.Help();
        } else if (myCmd.equals("pmread")) {
            int n = 0;
            if (args.length > 0) {
                try {
                    n = Integer.parseInt(args[0]);
                } catch (Exception ex) {
                    c.Close();
                    return false;
                }
                ret = c.Read(n);
            } else {
                ret = c.Read(n);
            }
        } else if (myCmd.equals("pmsend")) {
            if (args.length >= 3) {
                List<String> msg_text = new ArrayList<String>(Arrays.asList(args));
                ret = c.Send(args[0], args[1], msg_text.subList(2, msg_text.size()));
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