package red.man10.man10highlow.game;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import red.man10.man10highlow.util.JPYFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GameData implements Listener {

    public enum BetType{
        HIGH,
        LOW,
        DRAW
    }

    boolean isEnd = false;

    // 賭け金
    public int bet;

    // 賭け金の一時保管
    public long pot = 0;

    // 時間カウントのタスク
    BukkitTask timeTask;

    // ダイスその1
    int dice1 = 0;
    // ダイスその2
    int dice2 = 0;

    List<UUID> bet_high = new ArrayList<>(); // ハイに賭けたプレイヤーリスト
    List<UUID> bet_low = new ArrayList<>(); // ローに賭けたプレイヤーリスト
    List<UUID> bet_draw = new ArrayList<>(); // ドローに賭けたプレイヤーリスト

    private final GameManager manager;

    public GameData(GameManager manager, int bet){
        this.manager = manager;
        this.bet = bet;

        Bukkit.getServer().getPluginManager().registerEvents(this,manager.getPlugin());

        // ダイス1は先に決定
        dice1 = randomDice();

        sendCommandBroadCast(manager.getPlugin().prefix+"§e§l"+JPYFormat.getText(bet)+"円で§c§lハイ§a§lアンド§b§lロー§e§lの募集が開始されました！","§eクリックで開く！","/mhl");
        sendCommandBroadCast(manager.getPlugin().prefix+"§e§l100面ダイスの出目が §a§l"+dice1+" §e§lより §b§l多いか？§c§l少ないか？§a§l同じか！？","§eクリックで開く！","/mhl");
        sendCommandBroadCast(manager.getPlugin().prefix+"§e§l結果を予想してベットしよう！ §6§l[/mhl]","§eクリックで開く！","/mhl");

        timeTask = new BukkitRunnable(){
            int time = 120;

            @Override
            public void run() {

                if(isEnd){
                    cancel();
                    return;
                }
                time--;

                if(time==0){
                    // 抽選開始
                    cancel();
                    return;
                }

                // timeが20以上かつ20で割り切れるならBroadcast
                // つまり、100,80,60,40,20で発動
                if(time >= 20 && time % 20 == 0){
                    sendCommandBroadCast(manager.getPlugin().prefix+"§e§l"+time+"§e§l秒後、ベットが締め切られます！ §6§l[/mhl]","§eクリックで開く！","/mhl");
                }else if(time == 10 || time <= 5){ // timeが10か5以下ならBroadCast
                    sendCommandBroadCast(manager.getPlugin().prefix+"§e§lあと"+time+"§e§l秒でベットが締め切られます！ §6§l[/mhl]","§eクリックで開く！","/mhl");
                }
            }
        }.runTaskTimer(manager.getPlugin(),0,20);
    }

    // プレイヤーのベットを受け付ける
    // お金が絡むので排他処理
    public synchronized void playersBet(Player p,BetType betType){
        if(isPlayerJoined(p.getUniqueId())){
            // 既に参加済みなのでベットタイプを変更する
            if(getPlayerBetType(p.getUniqueId()).equals(betType)){
                // 元々のタイプと同一
                p.sendMessage(manager.getPlugin().prefix+"§c§lベット先は既に"+getTypeString(betType)+"§c§lです！");
                return;
            }
            removePlayer(p.getUniqueId());
            addPlayer(p.getUniqueId(),betType);
            playerBroadcast(manager.getPlugin().prefix+"§e§l"+p.getName()+"§a§lさんがベット先を"+getTypeString(betType)+"§a§lに変更しました！");
            information();
            return;
        }
        if(manager.getPlugin().vault.getBalance(p.getUniqueId())<bet){
            p.sendMessage(manager.getPlugin().prefix+"§c§l所持金が足りません！(必要: §e§l"+ JPYFormat.getText(bet)+"円§c§l)");
            return;
        }
        // お金を引き出す！！
        manager.getPlugin().vault.withdraw(p,bet);
        // ポットに追加！！
        pot += bet;
        // リストに追加！！
        addPlayer(p.getUniqueId(),betType);

        playerBroadcast(manager.getPlugin().prefix+"§e§l"+p.getName()+"§a§lさんが"+getTypeString(betType)+"§a§lにベットしました！");
        information();
    }

    // 結果抽選&発表～～～～！！！！
    public void startJudgement(){

    }

    // 途中何らかの理由でキャンセルする場合
    public void cancelGame(String reason){
        sendBroadCast(manager.getPlugin().prefix+"§c§l"+reason+"の理由でゲームがキャンセルされました。");
        isEnd = true;
        for(UUID uuid:bet_high){
            Player p = Bukkit.getPlayer(uuid);
            if(p==null){
                continue;
            }
            // ポットから取る
            pot -= bet;
            // お金を返却
            manager.getPlugin().vault.deposit(p,bet);
        }
        for(UUID uuid:bet_low){
            Player p = Bukkit.getPlayer(uuid);
            if(p==null){
                continue;
            }
            // ポットから取る
            pot -= bet;
            // お金を返却
            manager.getPlugin().vault.deposit(p,bet);
        }
        for(UUID uuid:bet_draw){
            Player p = Bukkit.getPlayer(uuid);
            if(p==null){
                continue;
            }
            // ポットから取る
            pot -= bet;
            // お金を返却
            manager.getPlugin().vault.deposit(p,bet);
        }

        bet_high.clear();
        bet_low.clear();
        bet_draw.clear();
        if(timeTask!=null&&!timeTask.isCancelled()){
            timeTask.cancel();
        }
        manager.endGame();
    }

    // プレイヤーが既にBETしているかどうか
    public boolean isPlayerJoined(UUID uuid){
        if(bet_high.contains(uuid)){
            return true;
        }
        if(bet_low.contains(uuid)){
            return true;
        }
        return bet_draw.contains(uuid);
    }

    // プレイヤーのBET先を取得
    public BetType getPlayerBetType(UUID uuid){
        if(bet_high.contains(uuid)){
            return BetType.HIGH;
        }
        if(bet_low.contains(uuid)){
            return BetType.LOW;
        }
        if(bet_draw.contains(uuid)){
            return BetType.DRAW;
        }
        return null;
    }

    // タイプをメッセージに適したStringとして取得
    public String getTypeString(BetType betType){
        switch (betType){
            case HIGH: {
                return "§b§lハイ";
            }
            case LOW: {
                return "§c§lロー";
            }
            case DRAW: {
                return "§a§lドロー";
            }
            default: {
                return "§CNO DATA";
            }
        }
    }

    public void information(){
        playerBroadcast("§b§lハイ§e:"+bet_high.size()+"人  "+
                "§c§lロー§e:"+bet_low.size()+"人  "+
                "§a§lドロー§e:"+bet_draw.size()+"人  §e§l合計賭け金:"+JPYFormat.getText(pot)+"円");
    }

    public void information(Player p){
        p.sendMessage("§b§lハイ§e:"+bet_high.size()+"人  "+
                "§c§lロー§e:"+bet_low.size()+"人  "+
                "§a§lドロー§e:"+bet_draw.size()+"人  §e§l合計賭け金:"+JPYFormat.getText(pot)+"円");
    }

    // ベットした人全員へメッセージを送信
    public void playerBroadcast(String msg){
        for(UUID uuid:bet_high){
            Player p = Bukkit.getPlayer(uuid);
            if(p==null){
                continue;
            }
            p.sendMessage(manager.getPlugin().prefix+"§r"+msg);
        }
        for(UUID uuid:bet_low){
            Player p = Bukkit.getPlayer(uuid);
            if(p==null){
                continue;
            }
            p.sendMessage(manager.getPlugin().prefix+"§r"+msg);
        }
        for(UUID uuid:bet_draw){
            Player p = Bukkit.getPlayer(uuid);
            if(p==null){
                continue;
            }
            p.sendMessage(manager.getPlugin().prefix+"§r"+msg);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //  マインクラフトチャットに、ホバーテキストや、クリックコマンドを設定する関数
    // [例1] sendHoverText(player,"ここをクリック",null,"/say おはまん");
    // [例2] sendHoverText(player,"カーソルをあわせて","ヘルプメッセージとか",null);
    // [例3] sendHoverText(player,"カーソルをあわせてクリック","ヘルプメッセージとか","/say おはまん");
    public void sendHoverText(Player p, String text, String hoverText, String command){
        //////////////////////////////////////////
        //      ホバーテキストとイベントを作成する
        HoverEvent hoverEvent = null;
        if(hoverText != null){
            BaseComponent[] hover = new ComponentBuilder(hoverText).create();
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover);
        }

        //////////////////////////////////////////
        //   クリックイベントを作成する
        ClickEvent clickEvent = null;
        if(command != null){
            clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,command);
        }

        BaseComponent[] message = new ComponentBuilder(text).event(hoverEvent).event(clickEvent). create();
        p.spigot().sendMessage(message);
    }

    // コマンド付きブロードキャスト
    public void sendCommandBroadCast(String text, String hoverText, String command){
        for(Player p : Bukkit.getOnlinePlayers()){
            sendHoverText(p,text,hoverText,command);
        }
    }

    // 通常ブロードキャスト
    public void sendBroadCast(String text){
        for(Player p : Bukkit.getOnlinePlayers()){
            p.sendMessage(text);
        }
    }

    // プレイヤーを追加
    private void addPlayer(UUID uuid,BetType betType){
        switch (betType){
            case HIGH: {
                bet_high.add(uuid);
                break;
            }
            case LOW: {
                bet_low.add(uuid);
                break;
            }
            case DRAW: {
                bet_draw.add(uuid);
                break;
            }
        }
    }

    // プレイヤーを削除
    private void removePlayer(UUID uuid){
        bet_high.remove(uuid);
        bet_low.remove(uuid);
        bet_draw.remove(uuid);
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event){
        Player p = event.getPlayer();
        if(isPlayerJoined(p.getUniqueId())){
            // リストから削除
            removePlayer(p.getUniqueId());
            // ポットから取る
            pot -= bet;
            // お金を返却
            manager.getPlugin().vault.deposit(p,bet);
            playerBroadcast("§e§l"+p.getName()+"§c§lはログアウトしたためベットが取り消されました。");
            information();
        }
    }

    // 100面ダイス
    public int randomDice(){
        Random rnd = new Random();
        return rnd.nextInt(100) + 1;
    }

    // 結果を取得
    public BetType getResult(){
        if(dice1>dice2){ //ロー
            return BetType.LOW;
        }else if(dice1<dice2){ //ハイ
            return BetType.HIGH;
        }else{ //ドロー
            return BetType.DRAW;
        }
    }
}
