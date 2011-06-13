/*
 * Commands actions of phpbbpm
 */
package fr.amazou.phpbbpm;

import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author fredericrousseau
 */
public class Commands {

    private SqlManager sql;
    private Player player;
    
    public Commands(Player player) {
        sql = new SqlManager();
        this.player = player;
        sql.setPlayer(player);
    }

    public boolean Read(int msg_id) {
        Map<String, String> msg = sql.ReadMsg(msg_id);
        try {
        if (msg == null || msg.size() == 0) {
            this.player.sendMessage(ChatColor.YELLOW + "No message");
        } else {
            this.player.sendMessage(String.format("%sde: %s%s", ChatColor.GREEN, ChatColor.WHITE,
                msg.get("username")));
            this.player.sendMessage(String.format("%sobj: %s%s", ChatColor.GREEN, ChatColor.WHITE,
                msg.get("message_subject")));
            this.player.sendMessage(String.format("%smsg: %s%s", ChatColor.GREEN, ChatColor.WHITE,
                msg.get("message_text")));
        }
        } catch (Exception ex) {
            Phpbbpm.getLog().info(ex.getMessage());
        }
        return true;
    }

    public boolean Send(String to, String subject, String text) {
        if (sql.SendMsg(to, subject, text)) {
            this.player.sendMessage(ChatColor.YELLOW + "Message sent to " + to);
        } else {
            this.player.sendMessage(ChatColor.YELLOW + "Message not sent");
        }
        return true;
    }

    public boolean List() {
        List<Map<String, String>> msgs = sql.getMsgs();
        
        if (msgs == null || msgs.size() == 0) {
            this.player.sendMessage(ChatColor.YELLOW + "No messages");
        } else {
            for (Map<String, String> elem : msgs) {
                this.player.sendMessage(String.format("#%s%s %s<%s> %s%s",
                    ChatColor.BLUE, elem.get("msg_id"), ChatColor.GREEN, elem.get("username"),
                    ChatColor.WHITE, elem.get("message_subject")));
            }
        }
        return true;
    }
    
    public void Help() {
        this.player.sendMessage(ChatColor.BLUE + "List of PhpbbPM cmds:");
        this.player.sendMessage(String.format("%s/pmlist                           "
                + "%s--list of unread pm", ChatColor.GREEN, ChatColor.GRAY));
        this.player.sendMessage(String.format("%s/pmread [id]                      "
                + "%s--read a pm. No id = newer pm", ChatColor.GREEN, ChatColor.GRAY));
        this.player.sendMessage(String.format("%s/pmsend <player> <subject> <msg>  "
                + "%s--send a pm", ChatColor.GREEN, ChatColor.GRAY));
    }
    
    public void Close() {
        sql.Close();
    }
}
