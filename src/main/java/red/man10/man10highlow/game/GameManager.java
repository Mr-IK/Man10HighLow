package red.man10.man10highlow.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import red.man10.man10highlow.Man10HighLow;
import red.man10.man10highlow.util.JPYFormat;

public class GameManager implements Listener {

    private Man10HighLow plugin;
    public GameData data = null;

    public GameManager(Man10HighLow plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    public void startGame(Player p,int bet){
        if(!getPlugin().power){
            p.sendMessage(plugin.prefix+"§c§lプラグインは停止中です！");
            return;
        }
        if(isGameStarted()){
            p.sendMessage(plugin.prefix+"§c§l既にゲームが開始されています！");
            return;
        }
        if(plugin.minbet>bet){
            p.sendMessage(plugin.prefix+"§c§l最低ベット金額(§e§l"+ JPYFormat.getText(plugin.minbet)+"円§c§l)を下回る金額では開始できません！");
            return;
        }
        if(plugin.maxbet<bet){
            p.sendMessage(plugin.prefix+"§c§l最大ベット金額(§e§l"+ JPYFormat.getText(plugin.maxbet)+"円§c§l)を上回る金額では開始できません！");
            return;
        }
        data = new GameData(this,bet);
    }

    public void startGame(Player p, int bet, int maxDice){
        if(!getPlugin().power){
            p.sendMessage(plugin.prefix+"§c§lプラグインは停止中です！");
            return;
        }
        if(isGameStarted()){
            p.sendMessage(plugin.prefix+"§c§l既にゲームが開始されています！");
            return;
        }
        if(plugin.minbet>bet){
            p.sendMessage(plugin.prefix+"§c§l最低ベット金額(§e§l"+ JPYFormat.getText(plugin.minbet)+"円§c§l)を下回る金額では開始できません！");
            return;
        }
        if(plugin.maxbet<bet){
            p.sendMessage(plugin.prefix+"§c§l最大ベット金額(§e§l"+ JPYFormat.getText(plugin.maxbet)+"円§c§l)を上回る金額では開始できません！");
            return;
        }
        if(maxDice<3){
            p.sendMessage(plugin.prefix+"§c§lダイスの最大値は3以上を指定してください！");
            return;
        }
        if(maxDice>10){
            p.sendMessage(plugin.prefix+"§c§lダイスの最大値は10以下を指定してください！");
            return;
        }
        data = new GameData(this,bet,maxDice);
    }

    public void playerBet(Player p, GameData.BetType type){
        if(!getPlugin().power){
            p.sendMessage(plugin.prefix+"§c§lプラグインは停止中です！");
            return;
        }
        if(!isGameStarted()){
            p.sendMessage(plugin.prefix+"§c§lゲームは募集されていません！");
            return;
        }
        // お金が絡むので一応排他処理
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
           data.playersBet(p,type);
        });
    }

    public boolean isGameStarted(){
        return data!=null;
    }

    public void endGame(){
        data = null;
    }

    public Man10HighLow getPlugin(){
        return this.plugin;
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event){
        if(isGameStarted()){
            data.logoutPlayer(event.getPlayer());
        }
    }
}
