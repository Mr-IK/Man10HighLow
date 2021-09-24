package red.man10.man10highlow.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import red.man10.man10highlow.Man10HighLow;
import red.man10.man10highlow.util.JPYFormat;

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
            p.sendMessage("§e/mhlop safeoff : 進行中のゲームをキャンセルせず、OFFします");
            p.sendMessage("§e/mhlop cancel (reason) : 進行中のゲームをキャンセルします");
            p.sendMessage("§e/mhlop setmin <Amount> : 最低ベット金額を<Amount>に設定します");
            p.sendMessage("§e/mhlop setmax <Amount> : 最大ベット金額を<Amount>に設定します");
            p.sendMessage("§e/mhlop reload : コンフィグをリロードします");
            p.sendMessage("§e/mhlop cashclear : キャッシュデータを削除しメモリを開放します");
            if(plugin.power){
                p.sendMessage("§a§lプラグインは動作中です。");
            }else{
                p.sendMessage("§c§lプラグインは停止中です。");
            }
        }else if(args.length==1) {
            if (args[0].equalsIgnoreCase("on")) {
                plugin.power = true;
                plugin.config.set("power", true);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix + "§aプラグインの動作をONしました。");

            } else if (args[0].equalsIgnoreCase("off")) {
                plugin.power = false;
                plugin.config.set("power", false);
                plugin.saveConfig();

                if (plugin.gameManager.isGameStarted()) {
                    plugin.gameManager.data.cancelGame("プラグイン停止");
                }

                p.sendMessage(plugin.prefix + "§aプラグインの動作をOFFしました。");
            } else if (args[0].equalsIgnoreCase("safeoff")) {
                plugin.power = false;
                plugin.config.set("power", false);
                plugin.saveConfig();

                p.sendMessage(plugin.prefix + "§aプラグインの動作をOFFしました。");

            } else if (args[0].equalsIgnoreCase("reload")) {

                plugin.configReload();

                p.sendMessage(plugin.prefix + "§aコンフィグをリロードしました。");

            } else if (args[0].equalsIgnoreCase("cashclear")) {

                plugin.userDataManager.cashClear();
                p.sendMessage(plugin.prefix + "§aキャッシュデータをクリアしました。");

            } else if (args[0].equalsIgnoreCase("cancel")) {
                if (!plugin.gameManager.isGameStarted()) {
                    p.sendMessage(plugin.prefix + "§cゲームは開かれていません。");
                    return true;
                }

                plugin.gameManager.data.cancelGame("運営による強制キャンセル");
                p.sendMessage(plugin.prefix + "§aゲームをキャンセルしました。");

            }
        }else if(args.length==2){
            if(args[0].equalsIgnoreCase("cancel")){
                if (!plugin.gameManager.isGameStarted()) {
                    p.sendMessage(plugin.prefix + "§cゲームは開かれていません。");
                    return true;
                }

                plugin.gameManager.data.cancelGame(args[1]);
                p.sendMessage(plugin.prefix + "§aゲームをキャンセルしました。");

            }else if(args[0].equalsIgnoreCase("setmin")){
                int min;

                try {

                    min = Integer.parseInt(args[1]);

                } catch (NumberFormatException mc) {
                    p.sendMessage(plugin.prefix + "§c金額は数字で指定してください。");
                    return true;
                }

                plugin.minbet = min;
                plugin.config.set("minbet",min);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix + "§a最低ベット金額を§e"+ JPYFormat.getText(min)+"円§aに設定しました。");
                return true;

            }else if(args[0].equalsIgnoreCase("setmax")){
                int max;

                try {

                    max = Integer.parseInt(args[1]);

                } catch (NumberFormatException mc) {
                    p.sendMessage(plugin.prefix + "§c金額は数字で指定してください。");
                    return true;
                }

                plugin.maxbet = max;
                plugin.config.set("maxbet",max);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix + "§a最大ベット金額を§e"+ JPYFormat.getText(max)+"円§aに設定しました。");
                return true;
            }
        }
        return true;
    }
}
