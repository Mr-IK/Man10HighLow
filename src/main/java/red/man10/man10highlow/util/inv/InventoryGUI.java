package red.man10.man10highlow.util.inv;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class InventoryGUI extends InventoryPlus implements Listener {

    // インベントリを閉じた際のイベント
    private List<Consumer<InventoryCloseEvent>> closeEvents = new ArrayList<>();

    // インベントリをクリックした際のイベント
    private List<Consumer<InventoryClickEvent>> clickEvents = new ArrayList<>();

    // インベントリの特定アイテムをクリックした際のイベント
    private HashMap<UUID,GUItem> guItemMap = new HashMap<>();

    // このインベントリを見ている/開いているユーザー
    private UUID openingPlayer = null;

    public InventoryGUI(String title, int size) {
        super(title, size);
    }

    // インベントリ閉じた時のイベントを追加
    public void addCloseEvent(Consumer<InventoryCloseEvent> event){
        closeEvents.add(event);
    }

    // インベントリ閉じた時のイベントを一気に追加
    public void setCloseEvents(List<Consumer<InventoryCloseEvent>> closeEvents){
        this.closeEvents.addAll(closeEvents);
    }

    // インベントリクリック時のイベントを追加
    public void addClickEvent(Consumer<InventoryClickEvent> event){
        clickEvents.add(event);
    }

    // インベントリクリック時のイベントを一気に追加
    public void setClickEvents(List<Consumer<InventoryClickEvent>> clickEvents){
        this.clickEvents.addAll(clickEvents);
    }

    // インベントリの状態を再読み込みする
    public void refreshRender(){
        super.refreshRender();
        if(openingPlayer==null){
            return;
        }
        Player p = Bukkit.getPlayer(openingPlayer);
        if(p==null){
            return;
        }
        for(int key: itemMap.keySet()){
            p.getOpenInventory().getTopInventory().setItem(key, itemMap.get(key));
        }
    }

    // 特殊アイテム一括登録
    public void setGUItemMap(HashMap<UUID,GUItem> guItemMap){
        this.guItemMap = guItemMap;
    }

    // 特殊アイテムを一気にセット
    public void setItems(int[] i, GUItem item){
        for(int ii :i){
            setItem(ii,item);
        }
    }

    // 特殊なアイテムで埋める
    public void fillInv(GUItem item){
        for(int i = 0;i<getSize();i++){
            setItem(i,item);
        }
    }

    // 特殊なアイテムのセット
    public void setItem(int i,GUItem item){
        itemMap.put(i,item.getItem());
        guItemMap.put(item.getUniqueID(),item);
    }

    // 特殊なアイテムを取得
    public GUItem getGUItem(int i){
        ItemStack item = itemMap.get(i);
        if(item!=null&&GUItem.isGUItem(item)){
            return guItemMap.get(GUItem.getGUItemID(item));
        }
        return null;
    }

    @Override
    public void open(Player p){
        if(openingPlayer!=null){
            return;
        }

        // インベントリを開く p.openInventory
        super.open(p);
        // イベント・プレイヤーを登録
        openingPlayer = p.getUniqueId();
        Bukkit.getServer().getPluginManager().registerEvents(this,GUICreator.getPlugin());
    }

    @Override
    public void clear(){
        super.clear();
        guItemMap.clear();
    }

    // 強制クローズ
    public void forceClose(Player p){
        if(!openingPlayer.equals(p.getUniqueId())){
            return;
        }
        Bukkit.getServer().getScheduler().runTask(GUICreator.getPlugin(), (Runnable) p::closeInventory);
    }

    // GUIをコピー
    @Override
    public InventoryGUI copy(){
        InventoryGUI copy = new InventoryGUI(getTitle(),getSize());
        copy.setItems(getItemsClone());
        List<Consumer<InventoryCloseEvent>> closeEvents = new ArrayList<>(this.closeEvents);
        copy.setCloseEvents(closeEvents);
        List<Consumer<InventoryClickEvent>> clickEvents = new ArrayList<>(this.clickEvents);
        copy.setClickEvents(clickEvents);
        HashMap<UUID,GUItem> guItemMap = new HashMap<>(this.guItemMap);
        copy.setGUItemMap(guItemMap);
        return copy;
    }

    // GUIのみをコピー (アイテムは無し)
    public InventoryGUI invCopy(){
        InventoryGUI copy = new InventoryGUI(getTitle(),getSize());
        List<Consumer<InventoryCloseEvent>> closeEvents = new ArrayList<>(this.closeEvents);
        copy.setCloseEvents(closeEvents);
        List<Consumer<InventoryClickEvent>> clickEvents = new ArrayList<>(this.clickEvents);
        copy.setClickEvents(clickEvents);
        return copy;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if(!openingPlayer.equals(e.getWhoClicked().getUniqueId())){
            return;
        }

        if(GUItem.isGUItem(itemMap.get(e.getSlot()))){
            for( Consumer<InventoryClickEvent> event : guItemMap.get(GUItem.getGUItemID(itemMap.get(e.getSlot()))).events){
                event.accept(e);
            }
        }

        for( Consumer<InventoryClickEvent> event : clickEvents){
            event.accept(e);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if(!openingPlayer.equals(e.getPlayer().getUniqueId())){
            return;
        }

        for( Consumer<InventoryCloseEvent> event : closeEvents){
            event.accept(e);
        }

        openingPlayer = null;
        HandlerList.unregisterAll(this);
    }
}
