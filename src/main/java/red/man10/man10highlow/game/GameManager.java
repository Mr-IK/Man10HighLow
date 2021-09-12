package red.man10.man10highlow.game;

import org.bukkit.entity.Player;
import red.man10.man10highlow.Man10HighLow;
import red.man10.man10highlow.util.JPYFormat;

public class GameManager {

    private Man10HighLow plugin;
    public GameData data = null;

    public GameManager(Man10HighLow plugin){
        this.plugin = plugin;
    }

    public void startGame(Player p,int bet){
        if(!getPlugin().power){
            p.sendMessage(plugin.prefix+"§c§lプラグインは停止中です！");
            return;
        }
        if(data!=null){
            p.sendMessage(plugin.prefix+"§c§l既にゲームが開始されています！");
            return;
        }
        if(plugin.minbet>bet){
            p.sendMessage(plugin.prefix+"§c§l最低BET金額(§e§l"+ JPYFormat.getText(plugin.minbet)+"円§c§l)を下回る金額ではスタートできません！");
            return;
        }
        if(plugin.maxbet<bet){
            p.sendMessage(plugin.prefix+"§c§l最大BET金額(§e§l"+ JPYFormat.getText(plugin.maxbet)+"円§c§l)を上回る金額ではスタートできません！");
            return;
        }
        p.sendMessage(plugin.prefix+"§aゲームを開始しています…§f§kAA");
        data = new GameData(this,bet);
    }

    public void endGame(){
        data = null;
    }

    public Man10HighLow getPlugin(){
        return this.plugin;
    }
}
