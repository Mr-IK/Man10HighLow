package red.man10.man10highlow.util.inv;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class GUItem {
    private final ItemStack item;
    private boolean clickable;

    private UUID unique_id;
    List<Consumer<InventoryClickEvent>> events = new ArrayList<>();

    public GUItem(ItemStack item){
        this.item = item;
        unique_id = UUID.randomUUID();

        ItemMeta meta = this.item.getItemMeta();
        meta.getPersistentDataContainer().set(GUICreator.getKey(), PersistentDataType.STRING, unique_id.toString());
        this.item.setItemMeta(meta);

        this.addEvent(event -> {
            if(!clickable) {
                event.setCancelled(true);
            }
        });
    }

    public GUItem addEvent(Consumer<InventoryClickEvent> consumer){
        events.add(consumer);
        return this;
    }

    public UUID getUniqueID() {
        return unique_id;
    }

    public boolean isClickable() {
        return clickable;
    }

    public GUItem setClickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }

    public ItemStack getItem() {
        return item;
    }

    public static boolean isGUItem(ItemStack item){
        return item.getItemMeta().getPersistentDataContainer().has(GUICreator.getKey(), PersistentDataType.STRING);
    }

    public static UUID getGUItemID(ItemStack item){
        if(!isGUItem(item)){
            return null;
        }
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        return UUID.fromString(container.get(GUICreator.getKey(), PersistentDataType.STRING));
    }
}
