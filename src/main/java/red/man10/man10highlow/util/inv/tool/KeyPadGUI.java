package red.man10.man10highlow.util.inv.tool;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import red.man10.man10highlow.util.inv.GUItem;
import red.man10.man10highlow.util.inv.ISBuilder;
import red.man10.man10highlow.util.inv.InventoryGUI;

import java.util.function.Consumer;

/*
 数字入力UIは極力似せたほうがいいと考え
 Shoさんのコード (https://github.com/shojabon/Man10ShopV2/blob/master/src/main/java/com/shojabon/man10shopv2/Utils/SInventory/ToolMenu/NumericInputMenu.java)
 を私のGUIシステムに合うようリビルドし、適用させました。この場を借りてお礼申し上げます。
 */

public class KeyPadGUI extends InventoryGUI {

    Consumer<Integer> enter;
    Consumer<InventoryClickEvent> cancel;
    Consumer<InventoryCloseEvent> close;

    BannerDictionary dictionary = new BannerDictionary();
    ItemStack information;

    int currentValue = 0;
    int maxValue = -1;
    int maxDigits = 9;
    boolean allowZero = true;

    int[] numberDisplay = new int[]{8,7,6,5,4,3,2,1,0};
    int[] numberPad = new int[]{46, 37, 38, 39, 28, 29, 30, 19, 20, 21};

    public KeyPadGUI(String title) {
        super(title, 54);
    }

    public void setEnter(Consumer<Integer> event){
        this.enter = event;
    }

    public void setCancel(Consumer<InventoryClickEvent> event){
        this.cancel = event;
    }

    public void setClose(Consumer<InventoryCloseEvent> event){
        this.close = event;
    }

    public void setInformation(ItemStack item){
        information = item;
    }

    public void setDefaultValue(int value){
        currentValue = value;
    }

    public void setMaxValue(int value){
        maxValue = value;
    }

    public void setMaxDigits(int value){
        if(value > 9) value = 9;
        if(value < 1) value = 1;
        maxDigits = value;
    }

    public void setAllowZero(boolean value){
        allowZero = value;
    }

    public void renderNumberPad(){
        for(int i = 0; i < 10; i++){
            GUItem item = new GUItem(ISBuilder.setNamed(dictionary.getItem(i),"§b§l"+i));
            item.setClickable(false);
            int nextNumber = i;

            item.addEvent(e -> {
                String currentString = String.valueOf(currentValue);


                //if starting with 0 change value
                if(currentValue == 0){
                    //if starting with 0 and next is also 0
                    if(nextNumber == 0){
                        return;
                    }
                    currentString = String.valueOf(nextNumber);
                }else{
                    currentString += nextNumber;
                }

                //if next value's digit is bigger than max
                if(currentString.length() > maxDigits) {
                    StringBuilder builder = new StringBuilder();
                    for(int ii = 0; ii < maxDigits; ii++){
                        builder.append("9");
                    }
                    currentValue = Integer.parseInt(builder.toString());
                    renderNumberDisplay();
                    return;
                }

                //if next value is bigger than max value
                if(Integer.parseInt(currentString) > maxValue && maxValue != -1){
                    currentValue = maxValue;
                    renderNumberDisplay();
                    return;
                }

                //finish
                currentValue = Integer.parseInt(currentString);
                renderNumberDisplay();
            });

            setItem(numberPad[i], item);
        }

        GUItem deleteInventoryItem = new GUItem( new ISBuilder(Material.TNT).setName("§4§lクリア").build());
        deleteInventoryItem.setClickable(false);
        deleteInventoryItem.addEvent(e -> {
            currentValue = 0;
            renderNumberDisplay();
        });
        setItem(48, deleteInventoryItem);
    }

    public void renderNumberDisplay(){
        // 表示位置を一度AIRに置き換える
        for(int i = 0; i < maxDigits; i++){
            setItem(numberDisplay[i], new ItemStack(Material.AIR));
        }

        // 現在の桁数を取得
        int lengthOfCurrentValue = String.valueOf(currentValue).length();
        // 桁数に応じてアイテムを再設置
        for(int i = 0; i < lengthOfCurrentValue; i++){
            int nextCharacter = Integer.parseInt(String.valueOf(String.valueOf(currentValue).charAt(i)));
            setItem(numberDisplay[lengthOfCurrentValue-1-i], ISBuilder.setNamed(dictionary.getItem(nextCharacter),"§b§l"+currentValue));
        }

        refreshRender();
    }



}
