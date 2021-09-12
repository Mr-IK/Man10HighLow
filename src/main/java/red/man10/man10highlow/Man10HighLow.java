package red.man10.man10highlow;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10highlow.command.HighLowCommand;
import red.man10.man10highlow.command.HighLowCommandOP;
import red.man10.man10highlow.game.GameManager;
import red.man10.man10highlow.user.UserDataManager;
import red.man10.man10highlow.util.VaultManager;
import red.man10.man10highlow.util.inv.GUICreator;
import red.man10.man10highlow.util.inv.GUItem;
import red.man10.man10highlow.util.inv.ISBuilder;
import red.man10.man10highlow.util.inv.InventoryGUI;

import java.util.UUID;

public final class Man10HighLow extends JavaPlugin {

    public GameManager gameManager;
    public UserDataManager userDataManager;
    public HighLowCommand command;
    public HighLowCommandOP commandOP;
    public final String prefix = "§f§l[§d§lMa§f§ln§a§l10§c§lハイ§e§l&§b§lロー§f§l]";

    public FileConfiguration config;
    public VaultManager vault;
    public boolean power = false;
    public int minbet = 1000;
    public int maxbet = 1000000;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = getConfig();
        vault = new VaultManager(this);
        power = config.getBoolean("power",false);
        minbet = config.getInt("minbet",1000);
        maxbet = config.getInt("maxbet",1000000);

        gameManager = new GameManager(this);
        userDataManager = new UserDataManager(this);
        command = new HighLowCommand(this);
        commandOP = new HighLowCommandOP(this);

        GUICreator.initGUICreator(this);

        getCommand("mhl").setExecutor(command);
        getCommand("mhlop").setExecutor(commandOP);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public UUID registerHighLowMenu(){
        InventoryGUI gui = GUICreator.createNewInv("§c§lハイ§a§lアンド§b§lロー §e§lメニュー",27);
        gui.fillInv(new GUItem(new ISBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("").build()).setClickable(false));

        gui.setItem(11,new GUItem(
                        new ISBuilder(Material.BELL)
                                .setName("§6§lゲームを開く")
                                .setLore(new String[]{"§e金額を指定し、ゲームを開きます！"})
                                .build()
                ).setClickable(false)
                .addEvent(e->{
                    // 金額指定UI
                })
        ); //ひだり
        gui.setItem(13,new GUItem(
                        new ISBuilder(Material.GOLDEN_SWORD)
                                .setName("§6§l最大獲得金額ランキングを見る")
                                .setLore(new String[]{"§e1戦で得た最大の獲得金額ランキングを表示します！"})
                                .setHideAttributes(true)
                                .build()
                ).setClickable(false)
                .addEvent(e->{
                    // チャット欄に表示かな？
                })
        ); //中央
        gui.setItem(15,new GUItem(
                        new ISBuilder(Material.GOLD_BLOCK)
                                .setName("§6§l合計獲得金額ランキングを見る")
                                .setLore(new String[]{"§e今までに得た獲得金額の総合ランキングを表示します！"})
                                .build()
                ).setClickable(false)
                .addEvent(e->{
                    // チャット欄に表示かな？
                })
        ); //みぎ
        gui.refreshRender();
        return GUICreator.registerInv(gui);
    }

    public UUID registerSetBETMenu(){
        InventoryGUI gui = GUICreator.createNewInv("§c§lハイ§a§lアンド§b§lロー §e§l金額指定",54);
        gui.fillInv(new GUItem(new ISBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("").build()).setClickable(false));

        return GUICreator.registerInv(gui);
    }
}
