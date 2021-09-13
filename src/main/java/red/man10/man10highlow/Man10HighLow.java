package red.man10.man10highlow;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10highlow.command.HighLowCommand;
import red.man10.man10highlow.command.HighLowCommandOP;
import red.man10.man10highlow.game.GameManager;
import red.man10.man10highlow.user.UserDataManager;
import red.man10.man10highlow.util.VaultManager;

public final class Man10HighLow extends JavaPlugin {

    public GameManager gameManager;
    public UserDataManager userDataManager;
    public HighLowCommand command;
    public HighLowCommandOP commandOP;
    public final String prefix = "§f§l[§d§lMa§f§ln§a§l10§c§lハイ§a§l&§b§lロー§f§l]";

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

        getCommand("mhl").setExecutor(command);
        getCommand("mhlop").setExecutor(commandOP);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void configReload(){
        reloadConfig();
        config = getConfig();
        if(power&&gameManager.isGameStarted()&&!config.getBoolean("power")){
            gameManager.data.cancelGame("プラグイン停止");
        }
        power = config.getBoolean("power",false);
        minbet = config.getInt("minbet",1000);
        maxbet = config.getInt("maxbet",1000000);
    }
}
