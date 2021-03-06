package red.man10.man10highlow.game;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import red.man10.man10highlow.user.UserData;
import red.man10.man10highlow.user.UserDataManager;
import red.man10.man10highlow.util.JPYFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GameData{

    public enum BetType{
        HIGH,
        LOW,
        DRAW
    }

    boolean isEnd = false;

    // ベットが許可されているか
    boolean betAllow = true;

    // 賭け金
    public int bet;

    // 賭け金の一時保管
    public long pot = 0;

    // 時間カウントのタスク
    BukkitTask timeTask;

    // ダイスの最大値
    // 実質的なデフォルトデータ
    public int maxDice = 30;
    // ダイスその1
    public int dice1 = 0;
    // ダイスその2
    int dice2 = 0;

    List<UUID> bet_high = new ArrayList<>(); // ハイに賭けたプレイヤーリスト
    List<UUID> bet_low = new ArrayList<>(); // ローに賭けたプレイヤーリスト
    List<UUID> bet_draw = new ArrayList<>(); // ドローに賭けたプレイヤーリスト

    private final GameManager manager;

    public GameData(GameManager manager, int bet){
        this.manager = manager;
        this.bet = bet;

        // ダイス1は先に決定
        dice1 = randomDice();

        sendCommandBroadCast(manager.getPlugin().prefix+" §f§l"+JPYFormat.getText(bet)+"円§e§lで§c§lハイ§a§lアンド§b§lロー§e§lの募集が開始されました！","§eクリックで開く！","/mhl");
        sendCommandBroadCast(manager.getPlugin().prefix+" §f§l"+maxDice+"§e§l面ダイスの出目が §a§l"+dice1+" §e§lより §b§l多いか？§c§l少ないか？§a§l同じか！？","§eクリックで開く！","/mhl");
        sendCommandBroadCast(manager.getPlugin().prefix+" §e§l結果を予想してベットしよう！ §6§l[/mhl]","§eクリックで開く！","/mhl");

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
                    betAllow = false;
                    startJudgement();
                    cancel();
                    return;
                }

                // timeが20以上かつ20で割り切れるならBroadcast
                // つまり、100,80,60,40,20で発動
                if(time >= 20 && time % 20 == 0){
                    sendCommandBroadCast(manager.getPlugin().prefix+" §f§l"+time+"§e§l秒後、ベットが締め切られます！ §6§l[/mhl]","§eクリックで開く！","/mhl");
                }else if(time == 10 || time <= 3){ // timeが10か3以下ならBroadCast
                    sendCommandBroadCast(manager.getPlugin().prefix+" §e§lあと§f§l"+time+"§e§l秒でベットが締め切られます！ §6§l[/mhl]","§eクリックで開く！","/mhl");
                }
            }
        }.runTaskTimer(manager.getPlugin(),0,20);
    }

    public GameData(GameManager manager, int bet, int maxDice){
        this.manager = manager;
        this.bet = bet;
        this.maxDice = maxDice;

        // ダイス1は先に決定
        dice1 = randomDice();

        sendCommandBroadCast(manager.getPlugin().prefix+" §f§l"+JPYFormat.getText(bet)+"円§e§lで§c§lハイ§a§lアンド§b§lロー§e§lの募集が開始されました！","§eクリックで開く！","/mhl");
        sendCommandBroadCast(manager.getPlugin().prefix+" §f§l"+maxDice+"§e§l面ダイスの出目が §a§l"+dice1+" §e§lより §b§l多いか？§c§l少ないか？§a§l同じか！？","§eクリックで開く！","/mhl");
        sendCommandBroadCast(manager.getPlugin().prefix+" §e§l結果を予想してベットしよう！ §6§l[/mhl]","§eクリックで開く！","/mhl");

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
                    betAllow = false;
                    startJudgement();
                    cancel();
                    return;
                }

                // timeが20以上かつ20で割り切れるならBroadcast
                // つまり、100,80,60,40,20で発動
                if(time >= 20 && time % 20 == 0){
                    sendCommandBroadCast(manager.getPlugin().prefix+" §f§l"+time+"§e§l秒後、ベットが締め切られます！ §6§l[/mhl]","§eクリックで開く！","/mhl");
                }else if(time == 10 || time <= 3){ // timeが10か3以下ならBroadCast
                    sendCommandBroadCast(manager.getPlugin().prefix+" §e§lあと§f§l"+time+"§e§l秒でベットが締め切られます！ §6§l[/mhl]","§eクリックで開く！","/mhl");
                }
            }
        }.runTaskTimer(manager.getPlugin(),0,20);
    }

    // プレイヤーのベットを受け付ける
    // お金が絡むので排他処理
    public synchronized void playersBet(Player p,BetType betType){
        if(!betAllow){
            p.sendMessage(manager.getPlugin().prefix+"§c§lベットは締め切られました");
            return;
        }
        if(isPlayerJoined(p.getUniqueId())){
            // 既に参加済みなのでベットタイプを変更する
            if(getPlayerBetType(p.getUniqueId()).equals(betType)){
                // 元々のタイプと同一
                p.sendMessage(manager.getPlugin().prefix+"§c§lベット先は既に"+getTypeString(betType)+"§c§lです！");
                return;
            }
            if(getChance(betType)==0){
                p.sendMessage(manager.getPlugin().prefix+"§c§l変更先の当選率は0%です！");
                return;
            }
            removePlayer(p.getUniqueId());
            addPlayer(p.getUniqueId(),betType);
            playerBroadcast(" §e§l"+p.getName()+"§a§lさんがベット先を"+getTypeString(betType)+"§a§lに変更しました！");
            information();
            return;
        }
        if(getChance(betType)==0){
            p.sendMessage(manager.getPlugin().prefix+"§c§lベット先の当選率は0%です！");
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

        playerBroadcast(" §e§l"+p.getName()+"§a§lさんが"+getTypeString(betType)+"§a§lにベットしました！");
        information();
    }

    // 結果抽選&発表～～～～！！！！
    public void startJudgement(){

        if(!isBattlePossible()){
            cancelGame("人数不足");
            return;
        }

        playerBroadcast(" §e§l賽は投げられた… §f§l§kAA");

        // 実はこの時点で結果は決まっているのだ
        dice2 = randomDice();

        // ﾄﾞｯｸﾝみたいな心拍音をプレイサウンド
        // 1秒、2秒、3秒で鳴らす。
        playerPlaySound(Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1,0.5f);
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(),()->{
            playerPlaySound(Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1,1f);
        },5);
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(),()->{
            playerPlaySound(Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1,0.5f);
        },20);
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(),()->{
            playerPlaySound(Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1,1f);
        },25);
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(),()->{
            playerPlaySound(Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1,0.5f);
        },40);
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(),()->{
            playerPlaySound(Sound.BLOCK_NOTE_BLOCK_BASEDRUM,1,1f);
        },45);

        // 1秒開けて、結果を発表
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(),()->{
            BetType result = getResult();
            sendBroadCast(manager.getPlugin().prefix+" §e§l結果は… §f§l"+dice2+"§e§l！ "+getTypeString(result)+"§e§lの勝利！");
            double winAmount = getWinAmount(result);
            List<UUID> winners;

            switch (result){
                case HIGH:{
                    winners = bet_high;
                    break;
                }

                case LOW:{
                    winners = bet_low;
                    break;
                }

                case DRAW:{
                    winners = bet_draw;
                    break;
                }

                default:{
                    winners = new ArrayList<>();
                }
            }

            if(winners.size()==0){
                sendBroadCast(manager.getPlugin().prefix+" §e§l…だが、誰も… "+getTypeString(result)+" §e§lにベットしていないのである！");
                playerBroadcast(" §e§l勝負は引き分けとし、賭け金は返却されます！");
                playerPlaySound(Sound.BLOCK_NOTE_BLOCK_BANJO,1,0.7f);
                playerPlaySound(Sound.BLOCK_NOTE_BLOCK_BANJO,1,0.95f);

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
                return;
            }

            for(UUID winner : winners){
                Player win = Bukkit.getPlayer(winner);
                if(win==null){
                    continue;
                }
                win.playSound(win.getLocation(),Sound.BLOCK_BELL_USE,1f,2f);
                manager.getPlugin().vault.deposit(win,winAmount);
                Bukkit.getScheduler().runTaskAsynchronously(manager.getPlugin(), ()->{
                    UserDataManager udm = manager.getPlugin().userDataManager;
                   if(udm.existsUserData(winner)){
                       // データが存在しているなら
                       UserData user = udm.getUserData(winner);
                       // 今までの最高獲得金額を更新していれば
                       if(user.getMaxWin()<winAmount){
                           win.sendMessage(manager.getPlugin().prefix+" §e§l最高獲得金額更新！おめでとう！ §f§l"+JPYFormat.getText(user.getMaxWin())+"円 §e§l⇒ §f§l"+JPYFormat.getText(winAmount)+"円");
                           udm.updateUserData(winner,(long)scaleCutDown(winAmount), (long)scaleCutDown(user.getTotalWin()+winAmount));
                       }else{
                           udm.updateUserData(winner,user.getMaxWin(), (long)scaleCutDown(user.getTotalWin()+winAmount));
                       }
                   }else{
                       // データがない場合
                       udm.addUserData(win,(long)scaleCutDown(winAmount),(long)scaleCutDown(winAmount));
                       win.sendMessage(manager.getPlugin().prefix+" §e§l初勝利おめでとうございます！ §6§l/mhl stats §e§lで最大/合計獲得金額を確認できるようになりました！");
                   }
                });
                playerBroadcast(" §f§l"+win.getName()+"§e§lさんは§f§l"+JPYFormat.getText(winAmount)+"円§e§lをゲットしました！");
            }

            isEnd = true;
            if(timeTask!=null&&!timeTask.isCancelled()){
                timeTask.cancel();
            }
            manager.endGame();
        },80);
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
        playerBroadcast("§b§lハイ§e:§f"+bet_high.size()+"人  "+
                "§c§lロー§e:§f"+bet_low.size()+"人  "+
                "§a§lドロー§e:§f"+bet_draw.size()+"人  §e§l合計賭け金:§f§l"+JPYFormat.getText(pot)+"円");
    }

    public void information(Player p){
        p.sendMessage("§b§lハイ§e:§f§l"+bet_high.size()+"人  "+
                "§c§lロー§e:§f§l"+bet_low.size()+"人  "+
                "§a§lドロー§e:"+bet_draw.size()+"人  §e§l合計賭け金:"+JPYFormat.getText(pot)+"円");
    }

    public void informationPlus(Player p){
        if(getChance(BetType.HIGH)==0){
            p.sendMessage("§b§l§mハイ§e§m:§f§m"+bet_high.size()+"人  §e§l§m当選確率: §f§l§m"+getChance(BetType.HIGH)+"%");
        }else{
            p.sendMessage("§b§lハイ§e:§f"+bet_high.size()+"人  §e§l当選確率: §f§l"+getChance(BetType.HIGH)+"%");
        }

        if(getChance(BetType.LOW)==0){
            p.sendMessage("§c§l§mロー§e§m:§f§m"+bet_low.size()+"人  §e§l§m当選確率: §f§l§m"+getChance(BetType.LOW)+"%");
        }else{
            p.sendMessage("§c§lロー§e:§f"+bet_low.size()+"人  §e§l当選確率: §f§l"+getChance(BetType.LOW)+"%");
        }

        if(getChance(BetType.DRAW)==0){
            p.sendMessage("§a§l§mドロー§e§m:§f§m"+bet_draw.size()+"人  §e§l§m当選確率: §f§l§m"+getChance(BetType.DRAW)+"%");
        }else{
            p.sendMessage("§a§lドロー§e:§f"+bet_draw.size()+"人  §e§l当選確率: §f§l"+getChance(BetType.DRAW)+"%");
        }
        p.sendMessage("§e§l合計賭け金:§f§l"+JPYFormat.getText(pot)+"円");
    }

    // ベットした人全員に音を鳴らす
    public void playerPlaySound(Sound sound,float volume,float pitch){
        for(UUID uuid:bet_high){
            Player p = Bukkit.getPlayer(uuid);
            if(p==null){
                continue;
            }
            p.playSound(p.getLocation(),sound,volume,pitch);
        }
        for(UUID uuid:bet_low){
            Player p = Bukkit.getPlayer(uuid);
            if(p==null){
                continue;
            }
            p.playSound(p.getLocation(),sound,volume,pitch);
        }
        for(UUID uuid:bet_draw){
            Player p = Bukkit.getPlayer(uuid);
            if(p==null){
                continue;
            }
            p.playSound(p.getLocation(),sound,volume,pitch);
        }
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

    public void logoutPlayer(Player p){
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

    // 1~maxダイス
    public int randomDice(){
        Random rnd = new Random();
        return rnd.nextInt(maxDice) + 1;
    }

    // ダイス 最小値と最大値はでない 2~Max-1
    public int randomDiceOne(){
        Random rnd = new Random();
        return rnd.nextInt((maxDice-2)) + 2;
    }

    // バトルができる状態(二種類以上のベットがされている)かどうか
    public boolean isBattlePossible(){
        if(bet_high.size()!=0&&bet_draw.size()!=0){
            return true;
        }else if(bet_high.size()!=0&&bet_low.size()!=0) {
            return true;
        }else return bet_draw.size() != 0 && bet_low.size() != 0;
    }

    // タイプ別当選確率を取得
    public double getChance(BetType betType){
        switch (betType){
            case HIGH: {
                // (最大ダイス-ダイス1) ÷ 最大ダイス * 100
                return scaleCut((double) (maxDice-dice1) / maxDice * 100);
            }
            case LOW: {
                // (ダイス1 -1) ÷ 最大ダイス * 100
                return scaleCut((double) (dice1-1) / maxDice * 100);
            }
            case DRAW: {
                // 1 ÷ 最大ダイス * 100
                return scaleCut((double) 1 / maxDice * 100);
            }
        }
        return -1;
    }

    // 一人当たりの当選金額を取得
    // potの金額 ÷ 当選人数
    // 小数点以下は切り捨て
    public double getWinAmount(BetType result){
        switch (result){
            case HIGH: {
                return scaleCutDown((double) pot / bet_high.size());
            }
            case LOW: {
                return scaleCutDown((double) pot / bet_low.size());
            }
            case DRAW: {
                return scaleCutDown((double) pot / bet_draw.size());
            }
        }
        return -1;
    }

    // 小数第一位までを切り捨て
    // 55.5555555...みたいなものが 55といった結果で帰ってくる
    public int scaleCutDown(double percent){
        return (int)Math.floor(percent);
    }

    // 小数第二位までを四捨五入
    // 33.333333...みたいなものが 33.3といった結果で帰ってくる
    public double scaleCut(double percent){
        BigDecimal bd = new BigDecimal(String.valueOf(percent));
        BigDecimal bd2 = bd.setScale(1, RoundingMode.HALF_UP);
        return bd2.doubleValue();
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
