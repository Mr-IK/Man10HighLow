package red.man10.man10highlow.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import red.man10.man10highlow.Man10HighLow;

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
        //インベントリひらこ～～
        return true;
    }
}
