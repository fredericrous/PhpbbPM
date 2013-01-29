/*
 * BroadCast unread messages for Phpbbpm
 */
package fr.amazou.phpbbpm;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * message broadcaster
 * 
 * @author Zougi
 */
class BroadCastUnread extends Phpbbpm {

    private String sign_msg;
    private String warn_msg;

    public BroadCastUnread() {
        Config config = Phpbbpm.getPluginConfig();
        sign_msg = config.getSignMsg();
        warn_msg = config.getWarnMsg();
    }

    /**
     * start the reminder scheduler
     */
    public void StartReminder() {

        Phpbbpm.getBukkitServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            public void run() {
                Player[] players = Phpbbpm.getBukkitServer().getOnlinePlayers();
                if (players.length > 0) {
                    SqlManager sql = new SqlManager();
                    Map<String, Integer> unread_msgs = sql.getNbUnreadMsg();
                    int pmNb = 0;
                    if (unread_msgs != null && unread_msgs.size() > 0)
                        for (Map.Entry<String, Integer> e : unread_msgs.entrySet()) {
                            Player p = findPlayer(e.getKey(), players);
                            pmNb = e.getValue();
                            if (p != null && pmNb != 0) {
                                p.sendMessage(String.format(warn_msg, ChatColor.RED, pmNb, ChatColor.WHITE));
                            }
                        }
                    sql.Close();
                }
            }

            /**
             * @return player if key is contained in players tab
             */
            private Player findPlayer(String key, Player[] players) {
                for (Player p : players) {
                    if (p.getName().toLowerCase().contains(key.toLowerCase())) {
                        return p;
                    }
                }
                return null;
            }
        }, 60L, 1200 * Phpbbpm.getPluginConfig().getWarnDelay());
    }

    /**
     * start the sign updater
     */
    public void StartSignUpdater() {

        Phpbbpm.getBukkitServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            public void run() {
                SqlManager sql = new SqlManager();
                List<Map<String, Object>> signs_list = sql.getSigns();
                Location sign_location;
                Sign sign;
                for (Map<String, Object> map : signs_list) {
                    sign_location = (Location) map.get("location");
                    sign = (Sign) sign_location.getBlock().getState();
                    sign.setLine(2, String.format(sign_msg, ChatColor.RED, Integer.parseInt(map.get("unread_msg").toString())));
                }
                sql.Close();
            }
        }, 60L, 1200 * Phpbbpm.getPluginConfig().getSignDelay());
    }

    /**
     * display a reminder on player join
     * 
     * @param p
     */
    public void JoinMessage(Player p) {
        SqlManager sql = new SqlManager();
        sql.setPlayer(p);
        int pmNb = sql.getNbUnreadMsg_solo();
        if (pmNb > 0) {
            p.sendMessage(String.format(warn_msg, ChatColor.RED, pmNb, ChatColor.WHITE));
        }
        sql.Close();
    }
}
