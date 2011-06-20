/*
 * Phpbbpm player listener
 */
package fr.amazou.phpbbpm;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * pbpbbpm playerjoin listener
 * @author Zougi
 */
public class PhpbbpmPlayerListener extends PlayerListener {

    public PhpbbpmPlayerListener() {
    }
    
    /**
     * display new pm count on player join
     * @param event 
     */
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        BroadCastUnread unread_msg = new BroadCastUnread();
        unread_msg.JoinMessage(event.getPlayer());
        //unread_msg.UpdateSign(event.getPlayer());
    }
}
