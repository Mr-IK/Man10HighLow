package red.man10.man10highlow.util.inv;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


// InventoryAPI V2
public class GUICreator {

    private static JavaPlugin plugin;

    // GUIのマップ UUIDはインベントリの固有ID(呼び出しに必要)
    private static HashMap<UUID,InventoryGUI> guiMap = new HashMap<>();

    // 現在開かれているGUIのリスト UUIDは上と違いプレイヤーのUUID
    private static HashMap<UUID,InventoryGUI> openingGUI = new HashMap<>();

    // GUI移動時Map削除を回避するためのリスト
    private static List<UUID> closeRemoveEscape = new ArrayList<>();

    // GUItemの生成時に使用する鍵
    private static NamespacedKey key;

    public static void initGUICreator(JavaPlugin plugin){
        GUICreator.plugin = plugin;
        key = new NamespacedKey(GUICreator.getPlugin(), "gui-creator-item");
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    // 新しくInventoryGUIを作成する
    public static InventoryGUI createNewInv(String title,int size){
        return new InventoryGUI(title,size);
    }

    // 設定が完了したInventoryGUIを登録する
    public static UUID registerInv(InventoryGUI gui){
        UUID unique_id = UUID.randomUUID();
        guiMap.put(unique_id,gui);
        return unique_id;
    }

    // IDに該当するInventoryGUIが存在するかどうか
    public static boolean containInv(UUID gui_id){
        return guiMap.containsKey(gui_id);
    }

    // インベントリGUIを開く
    public static void openGUI(Player p,UUID gui_id){
        if(!containInv(gui_id)){
            return;
        }
        if(isGUIOpen(p)){
            // 既に開いているためクローズイベントを回避するリストに追加
            closeRemoveEscape.add(p.getUniqueId());
        }
        // IDから取得したGUIをコピーする
        InventoryGUI gui = guiMap.get(gui_id).copy();
        // Mapに登録
        openingGUI.put(p.getUniqueId(),gui);
        // GUI閉じた時、Mapから削除
        gui.addCloseEvent(e -> {
            if(!closeRemoveEscape.contains(p.getUniqueId())){
                openingGUI.remove(p.getUniqueId());
            }else{
                closeRemoveEscape.remove(p.getUniqueId());
            }
        });

        // 開く
        gui.open(p);
    }

    // インベントリGUIを開く
    public static void openGUI(Player p,InventoryGUI gui){
        if(isGUIOpen(p)){
            // 既に開いているためクローズイベントを回避するリストに追加
            closeRemoveEscape.add(p.getUniqueId());
        }
        // Mapに登録
        openingGUI.put(p.getUniqueId(),gui);
        // GUI閉じた時、Mapから削除
        gui.addCloseEvent(e -> {
            if(!closeRemoveEscape.contains(p.getUniqueId())){
                openingGUI.remove(p.getUniqueId());
            }else{
                closeRemoveEscape.remove(p.getUniqueId());
            }
        });

        // 開く
        gui.open(p);
    }

    // プレイヤーはGUiを開いているかどうか
    public static boolean isGUIOpen(Player p){
        return openingGUI.containsKey(p.getUniqueId());
    }

    // インベントリGUIを閉じる
    public static void closeGUI(Player p){
        // 既に開いていない
        if(!openingGUI.containsKey(p.getUniqueId())){
            return;
        }
        InventoryGUI gui = openingGUI.get(p.getUniqueId());
        gui.forceClose(p);
    }


    public static NamespacedKey getKey() {
        return key;
    }
}
