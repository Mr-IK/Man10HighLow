package red.man10.man10highlow.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import red.man10.man10highlow.Man10HighLow;
import red.man10.man10highlow.game.GameData;
import red.man10.man10highlow.util.JPYFormat;

public class HighLowCommand implements CommandExecutor {

    private Man10HighLow plugin;

    public HighLowCommand(Man10HighLow plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            //CONSOLE向けコマンド
            return true;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("man10.highlow.use")){
            p.sendMessage("Unknown command. Type \"/help\" for help.");
            return true;
        }

        switch (args.length){
            case 0:{
                p.sendMessage("§e========"+plugin.prefix+"§e========");

                if(!plugin.power){
                    p.sendMessage("§c§lプラグインは停止中です。");
                    break;
                }

                p.sendMessage("§e/mhl open <金額> : ハイ&ローのゲームを開始します");
                p.sendMessage("§c/mhl high/h : ハイ(ダイス2が上)にベットします");
                p.sendMessage("§b/mhl low/l : ロー(ダイス2が下)にベットします");
                p.sendMessage("§b/mhl draw/d : ドロー(引き分け)にベットします");
                p.sendMessage("");

                // ゲームが開始されている場合、情報を表示
                if(plugin.gameManager.isGameStarted()){
                    p.sendMessage("§e§l"+ JPYFormat.getText(plugin.gameManager.data.bet)+"円 でゲームが募集中！");
                    plugin.gameManager.data.information(p);
                }else{
                    p.sendMessage("§c§l現在ゲームは開始されていません！");
                }

                break;
            }

            case 1:{

                // 85行目付近を参照
                playerBetCommand(p,args);

                break;
            }

            case 2:{

                if(args[0].equalsIgnoreCase("open")){

                    int bet;

                    try {

                        bet = Integer.parseInt(args[1]);

                    } catch (NumberFormatException mc) {
                        p.sendMessage(plugin.prefix + "§c§l金額は数字で指定してください。");
                        break;
                    }

                    plugin.gameManager.startGame(p,bet);

                    break;
                }

                break;
            }
        }
        return true;
    }

    public void playerBetCommand(Player p, String[] args){

        switch (args[0]){
            case "high":
            case "h":{

                // ハイにベット/変更
                plugin.gameManager.playerBet(p, GameData.BetType.HIGH);

                break;
            }

            case "low":
            case "l":{

                // ローにベット/変更
                plugin.gameManager.playerBet(p, GameData.BetType.LOW);

                break;
            }

            case "draw":
            case "d":{

                // ドローにベット/変更
                plugin.gameManager.playerBet(p, GameData.BetType.DRAW);

                break;
            }
        }

    }
}
