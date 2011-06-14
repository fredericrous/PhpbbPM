/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amazou.phpbbpm;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author fredericrousseau
 */
public class Listener extends PlayerListener {
    private static Phpbbpm plugin;
    
    public Listener(Phpbbpm instance) {
        plugin = instance;
    }
    
    public void onPlayerJoin(PlayerJoinEvent event) {
        BroadCastUnread unread_msg = new BroadCastUnread();
        unread_msg.JoinMessage(event.getPlayer());
    }
}
