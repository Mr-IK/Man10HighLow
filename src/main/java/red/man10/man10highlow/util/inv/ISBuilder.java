package red.man10.man10highlow.util.inv;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;


public class ISBuilder {

    private Material material;
    private int amount = 1;
    private String name = null;
    private String[] lore = new String[]{};
    private int damage = 0;
    private int customModel = 0;
    private boolean hideAttributes = false;
    private boolean unbreakable = false;
    private boolean enchant = false;

    public static ItemStack setNamed(ItemStack item,String name){
        ItemStack itemc = item.clone();
        ItemMeta meta = itemc.getItemMeta();
        meta.setDisplayName(name);
        itemc.setItemMeta(meta);
        return itemc;
    }

    public ISBuilder(Material material){
        this.material = material;
    }

    public ISBuilder(ItemStack item){
        this.material = item.getType();
        this.amount = item.getAmount();
        if(item.hasItemMeta()){
            if(item.getItemMeta().hasDisplayName()){
                name = item.getItemMeta().getDisplayName();
            }
            if(item.getItemMeta().hasLore()){
                lore = item.getItemMeta().getLore().toArray(new String[item.getItemMeta().getLore().size()]);
            }
            if(item.getItemMeta() instanceof Damageable){
                damage = ((Damageable)item.getItemMeta()).getDamage();
            }
            customModel = item.getItemMeta().getCustomModelData();
            hideAttributes = item.getItemFlags().contains(ItemFlag.HIDE_ATTRIBUTES);
            unbreakable = item.getItemMeta().isUnbreakable();
            enchant = item.getItemMeta().hasEnchants();
        }
    }

    public Material getMaterial() {
        return material;
    }

    public ISBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public ISBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public String getName() {
        return name;
    }

    public ISBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public String[] getLore() {
        return lore;
    }

    public ISBuilder setLore(String[] lore) {
        this.lore = lore;
        return this;
    }

    public int getDamage() {
        return damage;
    }

    public ISBuilder setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public int getCustomModel() {
        return customModel;
    }

    public ISBuilder setCustomModel(int customModel) {
        this.customModel = customModel;
        return this;
    }

    public boolean isHideAttributes() {
        return hideAttributes;
    }

    public ISBuilder setHideAttributes(boolean hideAttributes) {
        this.hideAttributes = hideAttributes;
        return this;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public ISBuilder setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public boolean isGlowing() {
        return enchant;
    }

    public ISBuilder setGlowing(boolean glowing) {
        this.enchant = glowing;
        return this;
    }

    public ItemStack build(){
        ItemStack item = new ItemStack(material,amount);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable){
            ((Damageable) meta).setDamage(damage);
        }
        meta.setCustomModelData(customModel);
        meta.setLore(Arrays.asList(lore));
        meta.setDisplayName(name);
        if(hideAttributes){
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        if(unbreakable){
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        if(enchant){
            meta.addEnchant(Enchantment.ARROW_FIRE,1,true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack itemFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items[0];
        } catch (Exception e) {
            return null;
        }
    }

    public static String itemToBase64(ItemStack item) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            ItemStack[] items = new ItemStack[1];
            items[0] = item;
            dataOutput.writeInt(items.length);

            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

}
