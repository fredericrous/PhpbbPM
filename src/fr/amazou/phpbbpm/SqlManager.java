/*
 * SqlManager for PhpbbPM
 */
package fr.amazou.phpbbpm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

/**
 *
 * @author fredericrousseau
 */
public class SqlManager extends Phpbbpm {
    
    private Connection conn = null;
    private Player player;
    private Logger log;
    private String db_prefix;
    
    public SqlManager() {
       Config config = Phpbbpm.getPluginConfig();
       log = Phpbbpm.getLog();
       
       String url = config.getDB_url();
       String db_driver = "com.mysql.jdbc.Driver";
       String db_name = config.getDB_name();
       String db_user = config.getDB_user(); 
       String db_pass = config.getDB_pass();
       db_prefix = config.getDB_prefix();

       try {
           Class.forName(db_driver).newInstance();
           conn = DriverManager.getConnection(url + db_name, db_user, db_pass);
           
       } catch (Exception e) {
           log.log(Level.WARNING, String.format("[phpbbpm] Error connecting to MySQL (%s),"
                   + " using login %s and password %s",
                   url + db_name, db_user, db_pass));
       }
    }
    
    public void setPlayer(Player p) {
       player = p;
    }
    
    public Map<String, Integer> getNbUnreadMsg() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        String query = String.format("select username, count(msg_id) as unread_msg"
                + " from %1$sprivmsgs_to msg"
                + " left join %1$susers u on msg.user_id = u.user_id"
                + " where pm_unread = 1 group by username", db_prefix);
        try {
            Statement st = conn.createStatement();
            ResultSet result = st.executeQuery(query);
            while (result.next()) {
                map.put(result.getString("username"), result.getInt("unread_msg"));
            }
        } catch (SQLException ex) {
            //log.log(Level.WARNING, "[phpbbpm] Error getting unreaded messages of players");
        }
        return map;
    }
    
    public int getNbUnreadMsg_solo() {
        String query = String.format("select count(msg_id) as unread_msg"
                + " from %1$sprivmsgs_to msg"
                + " left join %1$susers u on msg.user_id = u.user_id"
                + " where pm_unread = 1 and lower(username) like lower(?)", db_prefix);
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, "%" + player.getName() + "%");
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                return result.getInt("unread_msg");
            }
        } catch (SQLException ex) {
            //log.log(Level.WARNING, "[phpbbpm] Error getting unreaded message of " + player.getName());
        }
        return 0;
    }
    
    public List<Map<String, String>> getMsgs() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        String query = String.format(
                "select msgto.msg_id as msg_id, aut.username as username, message_subject, message_text"
                + " from %1$sprivmsgs_to msgto"
                + " left join %1$susers aut on msgto.author_id = aut.user_id"
                + " left join %1$sprivmsgs msg on msgto.msg_id = msg.msg_id"
                + " left join %1$susers u on msgto.user_id = u.user_id"
                + " where pm_unread = 1 and lower(u.username) like lower(?) limit 5", db_prefix);
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, "%" + player.getName() + "%");
            ResultSet result = ps.executeQuery();
            String[] keys = {"msg_id", "username",  "message_subject", "message_text"};
            Map<String, String> map;
            while(result.next()) {
                map = new HashMap<String, String>();
                for (String key : keys) {
                    map.put(key, result.getObject(key).toString());    
                }
                list.add(map);
            }
            return list;
        } catch (SQLException ex) {
            //log.log(Level.WARNING, "[phpbbpm] Error getting unreaded messages of " + player.getName());
        }
        
        return null;
    }
    
    public Map<String, String> ReadMsg(int msg_id) {
        Map<String, String> map = null;
        String query = String.format(
                "select msg.msg_id as msg_id, u.user_id as user_id,"
                + " aut.username as username, message_subject, message_text"
                + " from %1$sprivmsgs_to msgto"
                + " left join %1$susers aut on msgto.author_id = aut.user_id"
                + " left join %1$sprivmsgs msg on msgto.msg_id = msg.msg_id"
                + " left join %1$susers u on msgto.user_id = u.user_id"
                + " where pm_unread = 1 and lower(u.username) like lower(?)", db_prefix);
        if (msg_id != 0) {
            query += " and msgto.msg_id = ?";
        }
        query += " order by message_time desc limit 1";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, "%" + player.getName() + "%");
            if (msg_id != 0) {
                ps.setInt(2, msg_id);
            }
            ResultSet result = ps.executeQuery();
            String[] keys = {"username",  "message_subject", "message_text"};
            if (result.next()) {
                map = new HashMap<String, String>();
                for (String key : keys) {
                    map.put(key, result.getString(key));
                }
                if (map != null && map.size() > 0) {
                    conn.setAutoCommit(false);
                    
                    query = String.format("update %sprivmsgs_to set pm_unread = 0 where msg_id = %d",
                            db_prefix, result.getInt("msg_id"));
                    Statement st = conn.createStatement();
                    st.executeUpdate(query);
                    
                    query = String.format("update %susers", db_prefix)
                        + " set user_new_privmsg = case when user_new_privmsg > 0 then"
                        + " user_new_privmsg - 1 else 0 end,"
                        + " user_unread_privmsg = case when user_unread_privmsg > 0 then"
                        + " user_unread_privmsg - 1 else 0 end where user_id = "
                        + result.getInt("user_id");
                    st = conn.createStatement();
                    st.executeUpdate(query);
                    
                    conn.commit();
                    conn.setAutoCommit(true);
                }
            }
            return map;
        } catch (SQLException ex) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
                //this.log.info("[phpbbpm] Error updating new pm number");
            } catch (SQLException ex1) {
                //this.log.info("[phpbbpm] rollback error. ex: " + ex.getMessage());
            }
            //log.log(Level.WARNING, "[phpbbpm] Error getting unreaded message of " + player.getName());
        }
        return null;
    }
    
    public boolean SendMsg(String to, String msg_subject, List<String> msg_text) {
        String query_get_ids = String.format("select user_id from %susers", db_prefix)
                + " where lower(username) like lower(?)";
        query_get_ids += " union " + query_get_ids;
        
        String query_insert_pm = String.format("insert into %sprivmsgs", db_prefix)
                + " (author_id, message_time, message_subject, message_text, to_address, bcc_address)"
                + " values (?, UNIX_TIMESTAMP(), ?, ?, ?, ?)";
        
        String query_get_msg_id = "select @lastId := last_insert_id()";
        
        String query_pm_to = String.format("insert into %sprivmsgs_to", db_prefix)
                + " (msg_id, user_id, author_id, folder_id)"
                + " values (@lastId, ?, ?, -3)";
        
        String query_pm_to_me = String.format("insert into %sprivmsgs_to", db_prefix)
                + " (msg_id, user_id, author_id, pm_new, pm_unread, folder_id)"
                + " values (@lastId, ?, ?, 0, 0, -2)";
        
        String query_refresh_new_pm = String.format("update %susers", db_prefix)
                + " set user_new_privmsg = user_new_privmsg + 1,"
                + " user_unread_privmsg = user_unread_privmsg + 1 where user_id = ?";
        
        int to_id = 0;
        int author_id = 0;
        
        try {
                PreparedStatement ps_get_ids = conn.prepareStatement(query_get_ids);
                ps_get_ids.setString(1, "%" + player.getName() + "%");
                ps_get_ids.setString(2, "%" + to + "%");
                ResultSet result = ps_get_ids.executeQuery();
                result.next();
                author_id = result.getInt("user_id");
                result.next();
                to_id = result.getInt("user_id");
                conn.setAutoCommit(false);
                
                PreparedStatement ps_insert_pm = conn.prepareStatement(query_insert_pm);
                ps_insert_pm.setInt(1, author_id);
                ps_insert_pm.setString(2, msg_subject);
                String full_text = "";
                for (int i = 0; i < msg_text.size(); i++) {
                    full_text += msg_text.get(i);
                    if (i != msg_text.size() - 1) {
                       full_text += " "; 
                    }
                }
                ps_insert_pm.setString(3, full_text);
                ps_insert_pm.setString(4, "u_" + to_id);
                ps_insert_pm.setString(5, "");
                ps_insert_pm.executeUpdate();
                
                Statement st_get_msg_id = conn.createStatement();
                st_get_msg_id.execute(query_get_msg_id);

                PreparedStatement ps_pm_to = conn.prepareStatement(query_pm_to);
                ps_pm_to.setInt(1, to_id);
                ps_pm_to.setInt(2, author_id);
                ps_pm_to.executeUpdate();
                
                PreparedStatement ps_pm_to_me = conn.prepareStatement(query_pm_to_me);
                ps_pm_to_me.setInt(1, author_id);
                ps_pm_to_me.setInt(2, author_id);
                ps_pm_to_me.executeUpdate();
                
                PreparedStatement ps_refresh_new_pm = conn.prepareStatement(query_refresh_new_pm);
                ps_refresh_new_pm.setInt(1, to_id);
                ps_refresh_new_pm.executeUpdate();
                
                conn.commit();
                conn.setAutoCommit(true);
                return true;
        } catch (SQLException ex) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
                //this.log.info("[phpbbpm] Error sending message to " + to + "ex :" + ex.getMessage());
            } catch (SQLException ex1) {
                //this.log.info("[phpbbpm] rollback error. ex: " + ex.getMessage());
            }
        }
        return false;
    }
       
    public void Close() {
        try {
            conn.close();
        } catch (Exception e) {
            //log.log(Level.WARNING, "[phpbbpm] Error closing MySQL connection");
        }
    }
}
