package red.man10.man10highlow.util.inv;

import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.List;

public class BNBuilder extends ISBuilder{

    private List<Pattern> patterns;

    public BNBuilder(Material material) {
        super(material);
        patterns = new ArrayList<>();
    }

    public BNBuilder pattern(Pattern pattern) {
        patterns.add(pattern);
        return this;
    }

    public BNBuilder patterns(List<Pattern> patterns){
        this.patterns.addAll(patterns);
        return this;
    }

    @Override
    public ItemStack build(){
        ItemStack res = super.build();
        BannerMeta meta = (BannerMeta)res.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        for(Pattern pat : patterns){
            meta.addPattern(pat);
        }
        res.setItemMeta(meta);
        return res;
    }
}
