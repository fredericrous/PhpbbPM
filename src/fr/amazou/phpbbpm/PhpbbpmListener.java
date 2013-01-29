/*
 * Phpbbpm player listener
 */
package fr.amazou.phpbbpm;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * pbpbbpm playerjoin listener
 * 
 * @author Zougi
 */
public class PhpbbpmListener implements Listener {

    // private Logger log;
    private String sign_detect;
    private String sign_msg;
    private Phpbbpm plugin;

    public PhpbbpmListener(Phpbbpm plugin) {
        this.plugin = plugin;
        // log = Phpbbpm.getLog();
        sign_detect = Phpbbpm.getPluginConfig().getSignDetectionString();
        sign_msg = Phpbbpm.getPluginConfig().getSignMsg();
    }

    /**
     * display new pm count on player join
     * 
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        BroadCastUnread unread_msg = new BroadCastUnread(plugin);
        unread_msg.JoinMessage(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).trim().equalsIgnoreCase(sign_detect)) {
            SqlManager sql = new SqlManager();
            sql.setPlayer(event.getPlayer());
            if (sql.StoreSign(event.getBlock().getLocation())) {
                int pmNb = sql.getNbUnreadMsg_solo();
                event.setLine(2, String.format(sign_msg, ChatColor.RED, pmNb));
            } else {
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Can't store sign");
            }
            sql.Close();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Material mat = event.getBlock().getType();
        if ((mat.equals(Material.SIGN) || mat.equals(Material.SIGN_POST) || mat.equals(Material.WALL_SIGN))) {
            Sign sign = (Sign) event.getBlock().getState();
            if (sign.getLine(0).trim().equalsIgnoreCase(sign_detect)) {
                SqlManager sql = new SqlManager();
                sql.DeleteSign(sign.getBlock().getLocation());
                sql.Close();
            }
        }
    }

}
