package dev.mruniverse.guardianrftb.core.games;

import dev.mruniverse.guardianrftb.core.GuardianRFTB;
import dev.mruniverse.guardianrftb.core.enums.GuardianFiles;
import dev.mruniverse.guardianrftb.core.utils.TextUtilities;
import dev.mruniverse.guardianrftb.core.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class GameMenu {
    private final GuardianRFTB plugin;
    private final GameType gameType;
    private String name;
    private Inventory chestInventory;
    private String waiting;
    private String starting;
    private String playing;
    private String ending;
    private List<String> lore;
    private String iName;
    public GameMenu(GuardianRFTB plugin,GameType gameType) {
        this.plugin = plugin;
        this.gameType = gameType;
        createInv();
        loadItems();
    }
    private void createInv() {
        String invName = plugin.getStorage().getControl(GuardianFiles.MENUS).getString("menus.game.inventoryName");

        if(invName == null) invName = "&8Games";

        invName = ChatColor.translateAlternateColorCodes('&',invName);

        int rows = getRows(plugin.getStorage().getControl(GuardianFiles.MENUS).getInt("menus.game.inventoryRows"));

        chestInventory = plugin.getServer().createInventory(null,rows,invName);
    }
    private int getRows(int small) {
        if(small == 1) return 9;
        if(small == 2) return 18;
        if(small == 3) return 27;
        if(small == 4) return 36;
        if(small == 5) return 46;
        return 54;
    }
    private void pasteItems() {
        chestInventory.clear();
        int slot = 0;
        int maxSlot = chestInventory.getSize();
        for(Game game : plugin.getGameManager().getGames()) {
            if(game.getGameType() == gameType) {
                if (slot != maxSlot) {
                    ItemStack gameItem = getGameItem(game);
                    chestInventory.setItem(slot, gameItem);
                }
                slot++;
            }
        }
    }
    public void setSlots() {
        int slot = 0;
        for(Game game : plugin.getGameManager().getGames()) {
            if(game.getGameType() == gameType) {
                game.menuSlot = slot;
                slot++;
            }
        }
    }
    public void updateSlot(int slot,Game game) {
        if(slot != -1) {
            ItemStack gameItem = getGameItem(game);
            chestInventory.setItem(slot, gameItem);
            return;
        }
        setSlots();
    }
    public HashMap<ItemStack,String> getGameItems() {
        HashMap<ItemStack,String> hash = new HashMap<>();
        for(Game game : plugin.getGameManager().getGames()) {
            if(game.getGameType() == gameType) {
                ItemStack gameItem = getGameItem(game);
                hash.put(gameItem, game.getName());
            }
        }
        return hash;
    }
    private List<String> getLore(Game game) {
        List<String> newLore = new ArrayList<>();
        for(String line : lore) {
            String newLine = "&7" + line.replace("%map_name%", game.getName()).replace("%map_status%", game.gameStatus.getStatus()).replace("%map_mode%", game.getGameType().getType()).replace("%map_on%", game.getPlayers().size() + "").replace("%map_max%", game.max + "");
            newLore.add(newLine);
        }
        return TextUtilities.recolorLore(newLore);
    }
    private ItemStack getGameItem(Game game) {
        String name = iName.replace("%map_name%", game.getName()
                .replace("%map_status%", game.gameStatus.getStatus()
                        .replace("%map_mode%", game.getGameType().getType())
                        .replace("%map_on%", game.getPlayers().size() + "")
                        .replace("%map_max%", game.max + "")));
        switch (game.gameStatus) {
            case IN_GAME:
            case PLAYING:
                return getItem(playing,name,getLore(game));
            case STARTING:
                return getItem(starting,name,getLore(game));
            case PREPARING:
            case RESTARTING:
                return getItem(ending,name,getLore(game));
            case WAITING:
            default:
                return getItem(waiting,name,getLore(game));
        }
    }

    public void reloadMenu() {
        loadItems();
    }

    private void loadItems() {
        FileConfiguration loadConfig = plugin.getStorage().getControl(GuardianFiles.MENUS);
        try {
            String WaitingMaterial = loadConfig.getString("menus.game.item-status.waiting");
            String StartingMaterial = loadConfig.getString("menus.game.item-status.starting");
            String PlayingMaterial = loadConfig.getString("menus.game.item-status.playing");
            String EndingMaterial = loadConfig.getString("menus.game.item-status.ending");
            String itemName = loadConfig.getString("menus.game.item.name");

            List<String> itemLore = loadConfig.getStringList("menus.game.item.lore");

            if(WaitingMaterial == null) WaitingMaterial = "STAINED_CLAY:5";
            if(StartingMaterial == null) StartingMaterial = "STAINED_CLAY:4";
            if(PlayingMaterial == null) PlayingMaterial = "STAINED_CLAY:14";
            if(EndingMaterial == null) EndingMaterial = "STAINED_CLAY:3";
            if(itemName == null) itemName = "&e&nMap: %map_name%";

            lore = itemLore;
            iName = itemName;

            waiting = WaitingMaterial;
            starting = StartingMaterial;
            playing = PlayingMaterial;
            ending = EndingMaterial;

            pasteItems();
        } catch (Throwable throwable) {
            plugin.getLogs().error("Can't get chests items on startup");
            plugin.getLogs().error(throwable);
        }
    }
    private ItemStack getItem(String material,String itemName,List<String> itemLore) {
        Optional<XMaterial> optionalXMaterial = XMaterial.matchXMaterial(material);
        XMaterial m;
        if(optionalXMaterial.isPresent()) {
            m = optionalXMaterial.get();
            if (m.parseItem() != null) {
                ItemStack itemToReturn = m.parseItem();
                if(itemToReturn != null) {
                    ItemMeta ReturnMeta = itemToReturn.getItemMeta();
                    if(ReturnMeta != null) {
                        ReturnMeta.setDisplayName(TextUtilities.recolor(itemName));
                        ReturnMeta.setLore(TextUtilities.recolorLore(itemLore));
                        itemToReturn.setItemMeta(ReturnMeta);
                        return itemToReturn;
                    }
                    return itemToReturn;
                }
                return plugin.getNMSHandler().getItemStack(m.parseMaterial(), TextUtilities.recolor(itemName), TextUtilities.recolorLore(itemLore));
            }
            return null;
        }
        plugin.getLogs().error("Item: " + material + " doesn't exists.");
        return null;
    }
    public Inventory getInventory() {
        pasteItems();
        return chestInventory;
    }
    public void setName(String newName) { name = newName; }
    public String getName() { return name; }


}
