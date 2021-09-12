package red.man10.man10highlow.util.inv;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class InventoryPlus {

    private Inventory inv; //インベントリ

    private String title; // 通常タイトル
    private int size; //サイズ

    public HashMap<Integer,ItemStack> itemMap = new HashMap<>();

    public InventoryPlus(String title, int size){
        this.title = title;
        this.size = size;

        inv = Bukkit.getServer().createInventory(null, size, title);
    }

    // 元インベントリからアイテムをコピー
    public void inputItemFromInventory(Inventory inv){
        clear();
        for(int i = 0;i < this.inv.getSize();i++){
            if(inv.getItem(i)==null){
                continue;
            }
            ItemStack item = inv.getItem(i).clone();
            setItem(i, item);
        }
    }

    // インベントリの状態を再読み込みする
    public void refreshRender(){
        for(int key: itemMap.keySet()){
            inv.setItem(key, itemMap.get(key));
        }
    }

    // インベントリタイトルを更新
    public void setTitle(String title) {
        this.title = title;
        regenerateInventory();
    }

    // サイズを更新
    public void setSize(int size){
        this.size = size;
        regenerateInventory();
    }

    // キャッシュデータからインベントリを再生成
    public void regenerateInventory(){
        inv = Bukkit.getServer().createInventory(null, size, title);
        HashMap<Integer,ItemStack> mapCopy = getItemsClone();
        clear();
        for(int i = 0;i<inv.getSize();i++){
            inv.setItem(i,mapCopy.get(i));
        }
    }

    public InventoryPlus copy(){
        InventoryPlus copy = new InventoryPlus(title,size);
        HashMap<Integer,ItemStack> mapCopy = getItemsClone();
        for(int i = 0;i<copy.getSize();i++){
           copy.setItem(i,mapCopy.get(i));
        }
        return copy;
    }

    // アイテムをセット
    public void setItem(int i, ItemStack item){
        inv.setItem(i,item);
        itemMap.put(i,item);
    }

    // アイテムを取得
    public ItemStack getItem(int i){
        return inv.getItem(i);
    }

    // アイテムを一括取得
    public HashMap<Integer,ItemStack> getItemsClone(){
        return (HashMap<Integer,ItemStack>)itemMap.clone();
    }

    // アイテムを一気にセット
    public void setItems(int[] i, ItemStack item){
        for(int ii :i){
            setItem(ii,item);
        }
    }

    // アイテムを一気にセット
    public void setItems(HashMap<Integer,ItemStack> itemMap){
        for(int i = 0;i<itemMap.size();i++){
            setItem(i,itemMap.get(i));
        }
    }

    // インベントリを特定のアイテムで埋めつくす
    public void fillInv(ItemStack item){
        for(int i = 0;i<inv.getSize();i++){
            setItem(i,item);
        }
    }

    // インベントリのサイズを取得
    public int getSize(){
        return size;
    }

    public Inventory getInventory(){
        return inv;
    }

    // インベントリ(とアイテムデータ)をクリアー
    public void clear(){
        inv.clear();
        itemMap.clear();
    }

    // 通常のタイトル(固有IDなし)を取得
    public String getTitle() {
        return title;
    }

    // オープン
    public void open(Player p){
        p.openInventory(inv);
    }


}
