package red.man10.man10highlow.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import red.man10.man10highlow.Man10HighLow;
import red.man10.man10highlow.game.GameData;
import red.man10.man10highlow.user.UserData;
import red.man10.man10highlow.util.JPYFormat;

import java.util.HashMap;

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
                // ゲームが開始されている場合、情報を表示
                if(!plugin.power){
                    p.sendMessage("§c§lプラグインは停止中です。");
                    break;
                }else if(plugin.gameManager.isGameStarted()){
                    p.sendMessage("§f§l"+plugin.gameManager.data.maxDice+"§e§l面ダイス §a§l出目:"+plugin.gameManager.data.dice1+" §f§l"+ JPYFormat.getText(plugin.gameManager.data.bet)+"円 でゲームが募集中！");
                    plugin.gameManager.data.informationPlus(p);
                }else{
                    p.sendMessage("§c§l現在ゲームは開始されていません！");
                }
                p.sendMessage("");
                p.sendMessage("§e==========================");
                p.sendMessage("§e/mhl open <金額> (ダイス最大値) : ハイ&ローのゲームを開始します");
                p.sendMessage("§c/mhl high(h) : ハイ(ダイス2が上)にベットします");
                p.sendMessage("§b/mhl low(l) : ロー(ダイス2が下)にベットします");
                p.sendMessage("§a/mhl draw(d) : ドロー(引き分け)にベットします");
                p.sendMessage("§e/mhl stats : 自分の最高獲得金額や合計獲得金額を表示します");
                p.sendMessage("§e/mhl ranking max : 最大獲得金額のランキングを表示します");
                p.sendMessage("§e/mhl ranking total : 合計獲得金額のランキングを表示します");


                break;
            }

            case 1:{

                if(args[0].equalsIgnoreCase("stats")){

                    Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
                        if(!plugin.userDataManager.existsUserData(p.getUniqueId())){
                            p.sendMessage(plugin.prefix+"§c§l勝利したことがないため、データが存在しません");
                            return;
                        }
                        UserData user = plugin.userDataManager.getUserData(p.getUniqueId());
                        p.sendMessage(plugin.prefix+"§f§l【"+p.getName()+"】§e§l最大: §f§l"+JPYFormat.getText(user.getMaxWin())+"円  §e§l合計: §f§l"+JPYFormat.getText(user.getTotalWin())+"円");
                    });

                    break;
                }

                // 85行目付近を参照
                if(!playerBetCommand(p,args)){
                    p.sendMessage(plugin.prefix+"§c存在しないコマンドです！ /mhl で正しいコマンドを確認しましょう！");
                }

                break;
            }

            case 2:{

                if(args[0].equalsIgnoreCase("ranking")){

                    if(args[1].equalsIgnoreCase("max")){

                        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
                            p.sendMessage("");
                            p.sendMessage("§e§l========§6§l♚§c§lハイ§a§lアンド§b§lロー§6§l最大獲得金額ランキング♚§e§l========");
                            HashMap<Integer,UserData> maxMap = plugin.userDataManager.getMaxWinTop();
                            for(int i = 1;i<=10;i++){
                                if(!maxMap.containsKey(i)){
                                    break;
                                }
                                UserData user = maxMap.get(i);
                                p.sendMessage("§f§l"+i+"位【"+user.getName()+"】§e§l最大: §f§l"+JPYFormat.getText(user.getMaxWin())+"円  §e§l合計: §f§l"+JPYFormat.getText(user.getTotalWin())+"円");
                            }
                            p.sendMessage("");
                        });

                        break;

                    }else if(args[1].equalsIgnoreCase("total")){

                        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
                            p.sendMessage("");
                            p.sendMessage("§e§l========§6§l♚§c§lハイ§a§lアンド§b§lロー§6§l合計獲得金額ランキング♚§e§l========");
                            HashMap<Integer,UserData> totalMap = plugin.userDataManager.getTotalWinTop();
                            for(int i = 1;i<=10;i++){
                                if(!totalMap.containsKey(i)){
                                    break;
                                }
                                UserData user = totalMap.get(i);
                                p.sendMessage("§f§l"+i+"位【"+user.getName()+"】§e§l合計: §f§l"+JPYFormat.getText(user.getTotalWin())+"円  §e§l最大: §f§l"+JPYFormat.getText(user.getMaxWin())+"円");
                            }
                            p.sendMessage("");
                        });
                        break;

                    }

                }

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
                p.sendMessage(plugin.prefix+"§c存在しないコマンドです！ /mhl で正しいコマンドを確認しましょう！");
                break;
            }

            case 3:{

                if(args[0].equalsIgnoreCase("open")){

                    int bet;
                    int dice;

                    try {

                        bet = Integer.parseInt(args[1]);
                        dice = Integer.parseInt(args[2]);

                    } catch (NumberFormatException mc) {
                        p.sendMessage(plugin.prefix + "§c§l金額/最大ダイスは数字で指定してください。");
                        break;
                    }

                    plugin.gameManager.startGame(p,bet,dice);

                    break;
                }
                p.sendMessage(plugin.prefix+"§c存在しないコマンドです！ /mhl で正しいコマンドを確認しましょう！");
                break;
            }
            default:{
                p.sendMessage(plugin.prefix+"§c存在しないコマンドです！ /mhl で正しいコマンドを確認しましょう！");
            }
        }
        return true;
    }

    public boolean playerBetCommand(Player p, String[] args){

        switch (args[0]){
            case "high":
            case "h":{

                // ハイにベット/変更
                plugin.gameManager.playerBet(p, GameData.BetType.HIGH);

                return true;
            }

            case "low":
            case "l":{

                // ローにベット/変更
                plugin.gameManager.playerBet(p, GameData.BetType.LOW);

                return true;
            }

            case "draw":
            case "d":{

                // ドローにベット/変更
                plugin.gameManager.playerBet(p, GameData.BetType.DRAW);

                return true;
            }

            default:{
                return false;
            }
        }

    }
}
