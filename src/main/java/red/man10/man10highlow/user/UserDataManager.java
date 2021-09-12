package red.man10.man10highlow.user;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import red.man10.man10highlow.Man10HighLow;
import red.man10.man10highlow.util.sql.SQLManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class UserDataManager {

    private Man10HighLow plugin;
    private SQLManager sql;

    // プレイヤーのユーザーデータ
    private HashMap<UUID, UserData> userDataMap = new HashMap<>();

    public UserDataManager(Man10HighLow plugin){
        this.plugin = plugin;
        sql = new SQLManager(plugin,"Man10HighLow");
    }

    // ユーザーデータを作成
    public void addUserData(Player p){
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            sql.execute("INSERT INTO mhl_user_stats (name,uuid,max_win,total_win) " +
                    "VALUES ('"+p.getName()+"','"+p.getUniqueId().toString()+"',0,0);");
            UserData user = new UserData(p.getUniqueId(),p.getName(),0L,0L);
            userDataMap.put(p.getUniqueId(),user);
        });
    }

    // ユーザーデータを更新
    public void updateItem(UUID uuid,Long max_win,Long total_win){
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            if(!existsUserData(uuid)){
                return;
            }
            sql.execute("UPDATE mhl_user_stats SET max_win = "+max_win+" , total_win = "+total_win+" WHERE uuid = '"+uuid.toString()+"';");
            UserData userData = getUserData(uuid);
            userData.setMaxWin(max_win);
            userData.setTotalWin(total_win);
            userDataMap.put(uuid,userData);
        });
    }

    // ユーザーデータを取得
    public UserData getUserData(UUID uuid){

        if(userDataMap.containsKey(uuid)){
            return userDataMap.get(uuid);
        }

        String exe = "SELECT * FROM mhl_user_stats WHERE uuid = '"+uuid.toString()+"';";
        SQLManager.Query qu = sql.query(exe);
        if(!sql.checkQuery(qu)){
            return null;
        }
        ResultSet rs = qu.getRs();
        try {
            if(rs.next()) {
                UserData user = new UserData(uuid,rs.getString("name"),rs.getLong("max_win"),rs.getLong("total_win"));
                qu.close();
                return user;
            }
            qu.close();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            qu.close();
            return null;
        }
    }

    // 最大獲得金額 = Max_winが大きい順に取得
    // 登録は1から10まで
    public HashMap<Integer,UserData> getMaxWinTop(){
        HashMap<Integer,UserData> maxWinMap = new HashMap<>();

        String exe = "SELECT * FROM mhl_user_stats ORDER BY max_win DESC LIMIT 10;";
        SQLManager.Query qu = sql.query(exe);
        if(!sql.checkQuery(qu)){
            return maxWinMap;
        }
        ResultSet rs = qu.getRs();
        try {
            int i = 1;
            while(rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                UserData user = new UserData(uuid,rs.getString("name"),rs.getLong("max_win"),rs.getLong("total_win"));
                maxWinMap.put(i,user);
                i++;
            }
            qu.close();
            return maxWinMap;
        } catch (SQLException e) {
            e.printStackTrace();
            qu.close();
            return maxWinMap;
        }
    }

    // 合計獲得金額 = Total_winが大きい順に取得
    // 登録は1から10まで
    public HashMap<Integer,UserData> getTotalWinTop(){
        HashMap<Integer,UserData> totalWinMap = new HashMap<>();

        String exe = "SELECT * FROM mhl_user_stats ORDER BY total_win DESC LIMIT 10;";
        SQLManager.Query qu = sql.query(exe);
        if(!sql.checkQuery(qu)){
            return totalWinMap;
        }
        ResultSet rs = qu.getRs();
        try {
            int i = 1;
            while(rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                UserData user = new UserData(uuid,rs.getString("name"),rs.getLong("max_win"),rs.getLong("total_win"));
                totalWinMap.put(i,user);
                i++;
            }
            qu.close();
            return totalWinMap;
        } catch (SQLException e) {
            e.printStackTrace();
            qu.close();
            return totalWinMap;
        }
    }

    // ユーザーデータは存在するか
    public boolean existsUserData(UUID uuid){

        // キャッシュデータに存在している
        if(userDataMap.containsKey(uuid)){
            return true;
        }

        String exe = "SELECT * FROM mhl_user_stats WHERE uuid = '"+uuid.toString()+"';";
        SQLManager.Query qu = sql.query(exe);
        if(!sql.checkQuery(qu)){
            return false;
        }
        ResultSet rs = qu.getRs();
        try {
            if(rs.next()) {
                qu.close();
                return true;
            }
            qu.close();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            qu.close();
            return false;
        }
    }


}
