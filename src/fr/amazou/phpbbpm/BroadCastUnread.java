/*
 * BroadCast unread messages for Phpbbpm
 */
package fr.amazou.phpbbpm;

import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

/**
 *
 * @author fredericrousseau
 */
class BroadCastUnread extends Phpbbpm {

    public BroadCastUnread() {
        
    }
    
    public void Start() {
       
        Phpbbpm.getBukkitServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

            public void run() {
                Player[] players = Phpbbpm.getBukkitServer().getOnlinePlayers();
                if (players.length > 0) {
                    SqlManager sql = new SqlManager();
                    Map<String, Integer> unread_msgs = sql.getNbUnreadMsg();
                    int pmNb = 0;
                    if (unread_msgs != null && unread_msgs.size() > 0)
                    for (Map.Entry<String, Integer> e : unread_msgs.entrySet()){
                        Player p = findPlayer(e.getKey().toLowerCase(), players);
                        pmNb = e.getValue();
                        if (p != null && pmNb != 0) {
                            p.sendMessage(String.format("%s%s %spm unread.", ChatColor.RED, pmNb, ChatColor.WHITE));
                        }
                    }
                    sql.Close();
                }
            }

            private Player findPlayer(String key, Player[] players) {
                for (Player p : players) {
                    if (p.getName().toLowerCase().contains(key)) {
                        return p;
                    }
                }
                return null;
            }
        }, 60L, 1200 * Phpbbpm.getPluginConfig().getDelay());
    }
    
    public void JoinMessage(Player p) {
        SqlManager sql = new SqlManager();
        sql.setPlayer(p);
        int pmNb = sql.getNbUnreadMsg_solo();
        if (pmNb > 0) {
            p.sendMessage(String.format("%s%s %spm unread.", ChatColor.RED, pmNb, ChatColor.WHITE));
        }
        sql.Close();
    }
}
