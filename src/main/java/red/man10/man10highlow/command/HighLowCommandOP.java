package red.man10.man10highlow.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import red.man10.man10highlow.Man10HighLow;

public class HighLowCommandOP implements CommandExecutor {

    private Man10HighLow plugin;

    public HighLowCommandOP(Man10HighLow plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            //CONSOLE向けコマンド
            return true;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("man10.highlow.op")){
            p.sendMessage("Unknown command. Type \"/help\" for help.");
            return true;
        }
        if(args.length==0){
            p.sendMessage("§e========"+plugin.prefix+"§e========");
            p.sendMessage("§e/mhlop on/off : プラグインの動作をON/OFFします");
            p.sendMessage("§e/mhlop setmin <Amount> : 最低BET金額を<Amount>に設定します");
            p.sendMessage("§e/mhlop setmax <Amount> : 最大BET金額を<Amount>に設定します");
            if(plugin.power){
                p.sendMessage("§a§lプラグインは動作中です。");
            }else{
                p.sendMessage("§c§lプラグインは停止中です。");
            }
        }else if(args.length==1){
            if(args[0].equalsIgnoreCase("on")){
                plugin.power = true;
                plugin.config.set("power",true);
                p.sendMessage(plugin.prefix+"§aプラグインの動作をONしました。");
                //ここでBroadcast? まあいらないか
            }else if(args[0].equalsIgnoreCase("off")){
                plugin.power = false;
                plugin.config.set("power",false);

                if(plugin.gameManager.data!=null){
                    plugin.gameManager.data.cancelGame("プラグイン停止");
                }

                p.sendMessage(plugin.prefix+"§aプラグインの動作をOFFしました。");
                //ここでBroadcast? いらないか
            }
        }
        return true;
    }
}
