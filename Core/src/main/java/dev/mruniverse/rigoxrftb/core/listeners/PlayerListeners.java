package dev.mruniverse.rigoxrftb.core.listeners;

import dev.mruniverse.rigoxrftb.core.enums.CurrentItem;
import dev.mruniverse.rigoxrftb.core.enums.Files;
import dev.mruniverse.rigoxrftb.core.enums.RigoxBoard;
import dev.mruniverse.rigoxrftb.core.RigoxRFTB;
import dev.mruniverse.rigoxrftb.core.enums.SaveMode;
import dev.mruniverse.rigoxrftb.core.games.Game;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerListeners implements Listener {
    private final RigoxRFTB plugin;
    public PlayerListeners(RigoxRFTB main) {
        plugin = main;
        main.getLogs().info("PlayerListener registered!");
    }
    @EventHandler
    public void joinOptions(PlayerJoinEvent event) {
        plugin.addPlayer(event.getPlayer());
        FileConfiguration file = plugin.getFiles().getControl(Files.SETTINGS);
        Player player = event.getPlayer();
        if(file.getBoolean("settings.options.hideServerJoinMessage")) {
            event.setJoinMessage(null);
        }
        if(file.getBoolean("settings.options.joinHeal")) {
            player.setHealth(20.0D);
            player.setFoodLevel(20);
            player.setLevel(0);
            player.setExp(0.0F);
        }
        if(file.getBoolean("settings.options.joinAdventureGamemode")) {
            player.setGameMode(GameMode.ADVENTURE);
        }
        if(file.getBoolean("settings.options.clearInventory-onJoin")) {
            player.getInventory().clear();
        }
        for(ItemStack item : plugin.getLobbyItems().keySet()) {
            player.getInventory().setItem(plugin.getSlot(item),item);
        }
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getItem() != null) {
            if(event.getItem() == null) return;
            if(event.getItem().getItemMeta() == null) return;
            if(event.getItem().getType().equals(plugin.exitItem.getType()) && event.getItem().getItemMeta().equals(plugin.exitItem.getItemMeta())) {
                event.setCancelled(true);
                plugin.getPlayerData(event.getPlayer().getUniqueId()).getGame().leave(player);
                return;
            }

            for(ItemStack item : plugin.getLobbyItems().keySet()) {
                if(event.getItem().getType().equals(item.getType()) && event.getItem().getItemMeta().equals(item.getItemMeta())) {
                    CurrentItem itemAction = plugin.getCurrent(item);
                    event.setCancelled(true);
                    if(itemAction.equals(CurrentItem.EXIT_GAME)) {
                        if(plugin.getPlayerData(event.getPlayer().getUniqueId()).getGame() != null) {
                            plugin.getPlayerData(event.getPlayer().getUniqueId()).getGame().leave(player);
                            return;
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void joinScoreboard(PlayerJoinEvent event) {
        try {
            FileConfiguration file = plugin.getFiles().getControl(Files.SETTINGS);
            if (file.getBoolean("settings.lobbyScoreboard-only-in-lobby-world")) {
                if (file.getString("settings.lobbyLocation").equalsIgnoreCase("notSet")) {
                    plugin.getLogs().error("-----------------------------");
                    plugin.getLogs().error("Can't show lobby-scoreboard, lobby location is not set");
                    plugin.getLogs().error("-----------------------------");
                } else {
                    String[] loc = file.getString("settings.lobbyLocation").split(",");
                    World w = Bukkit.getWorld(loc[0]);
                    if (event.getPlayer().getWorld().equals(w)) {
                        if(plugin.getFiles().getControl(Files.SCOREBOARD).getBoolean("scoreboards.lobby.toggle")) {
                            plugin.getScoreboards().setScoreboard(RigoxBoard.LOBBY,event.getPlayer());
                        }
                    }
                }
            } else {
                if(plugin.getFiles().getControl(Files.SCOREBOARD).getBoolean("scoreboards.lobby.toggle")) {
                    plugin.getScoreboards().setScoreboard(RigoxBoard.LOBBY,event.getPlayer());
                }
            }
        } catch (Throwable throwable) {
            plugin.getLogs().error("Can't generate lobby scoreboard for " + event.getPlayer().getName() +"!");
            plugin.getLogs().error(throwable);
        }
    }
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if(plugin.getPlayerData(event.getPlayer().getUniqueId()).getGame() != null) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if(plugin.getPlayerData(event.getPlayer().getUniqueId()).getGame() != null) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        plugin.getScoreboards().removeScore(event.getPlayer());
        if(plugin.getPlayerData(event.getPlayer().getUniqueId()).getGame() != null) {
            plugin.getPlayerData(event.getPlayer().getUniqueId()).getGame().leave(event.getPlayer());
        }
        plugin.removePlayer(event.getPlayer());
        plugin.getNMSHandler().deleteBossBar(event.getPlayer());
        if(plugin.getFiles().getControl(Files.SETTINGS).getBoolean("settings.options.hideServerQuitMessage")) {
            event.setQuitMessage(null);
        }
    }
    @EventHandler
    public void inGameDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        if(plugin.getPlayerData(player.getUniqueId()).getGame() != null) {
            Game game = plugin.getPlayerData(player.getUniqueId()).getGame();
            event.getDrops().clear();
            event.setDeathMessage(null);
            event.setDroppedExp(0);
            if(game.beasts.contains(player)) {
                player.spigot().respawn();
                player.setGameMode(GameMode.SPECTATOR);
                game.deathBeast(player);
                player.teleport(game.beastLocation);
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                player.spigot().respawn();
                game.deathRunner(player);
                player.teleport(game.runnersLocation);
                player.setGameMode(GameMode.SPECTATOR);

            }
            player.setGameMode(GameMode.SPECTATOR);
        }
    }
    @EventHandler
    public void lobbyDamage(EntityDamageEvent event) {
        if(event.getEntity().getType().equals(EntityType.PLAYER)) {
            if (!plugin.getFiles().getControl(Files.SETTINGS).getString("settings.lobbyLocation").equalsIgnoreCase("notSet")) {
                String[] loc = plugin.getFiles().getControl(Files.SETTINGS).getString("settings.lobbyLocation").split(",");
                World w = Bukkit.getWorld(loc[0]);
                if (event.getEntity().getWorld().equals(w)) {
                    event.setCancelled(true);
                }
            }
            Player player = (Player)event.getEntity();
            if(plugin.getPlayerData(player.getUniqueId()).getGame() != null) {
                RigoxBoard board = plugin.getPlayerData(player.getUniqueId()).getBoard();
                if(board.equals(RigoxBoard.WAITING) || board.equals(RigoxBoard.STARTING) || board.equals(RigoxBoard.WIN_BEAST_FOR_BEAST) || board.equals(RigoxBoard.WIN_BEAST_FOR_RUNNERS) || board.equals(RigoxBoard.WIN_RUNNERS_FOR_BEAST) || board.equals(RigoxBoard.WIN_RUNNERS_FOR_RUNNERS)) {
                    event.setCancelled(true);
                } else {
                    if(event.getCause().equals(EntityDamageEvent.DamageCause.FALL) || plugin.getPlayerData(player.getUniqueId()).getGame().spectators.contains(player)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    @EventHandler
    public void inGameDamage(EntityDamageByEntityEvent event) {
        if(event.getEntity().getType().equals(EntityType.PLAYER)) {
            if(event.getDamager().getType().equals(EntityType.PLAYER)) {
                Player victim = (Player)event.getEntity();
                Player attacker = (Player)event.getDamager();
                if(plugin.getPlayerData(victim.getUniqueId()).getGame() != null) {
                    if(plugin.getPlayerData(attacker.getUniqueId()).getGame() == null) {
                        event.setCancelled(true);
                    } else {
                        Game game = plugin.getPlayerData(victim.getUniqueId()).getGame();
                        if(game.runners.contains(victim) && game.runners.contains(attacker)) {
                            event.setCancelled(true);
                        }
                    }
                }

            }
        }
    }
    @EventHandler
    public void lobbyHunger(FoodLevelChangeEvent event) {
        if(event.getEntity().getType().equals(EntityType.PLAYER)) {
            if (!plugin.getFiles().getControl(Files.SETTINGS).getString("settings.lobbyLocation").equalsIgnoreCase("notSet")) {
                String[] loc = plugin.getFiles().getControl(Files.SETTINGS).getString("settings.lobbyLocation").split(",");
                World w = Bukkit.getWorld(loc[0]);
                if (event.getEntity().getWorld().equals(w)) {
                    event.setFoodLevel(20);
                }
            }
            Player player = (Player)event.getEntity();
            if(plugin.getPlayerData(player.getUniqueId()).getGame() != null) {
                event.setFoodLevel(20);
            }
        }
    }
    @EventHandler
    public void onSignCreate(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("RigoxRFTB.admin.signCreate"))
            return;
        try {
            String line1 = event.getLine(0);
            if(line1 == null) return;
            if (line1.equalsIgnoreCase("[RFTB]")) {
                String name = event.getLine(1);
                if(name == null) name = "null";
                final Game game = plugin.getGameManager().getGame(name);
                if (game == null) {
                    String errorMsg = plugin.getFiles().getControl(Files.MESSAGES).getString("messages.admin.arenaError");
                    if(errorMsg == null) errorMsg = "&c%arena_id% don't exists";
                    errorMsg = errorMsg.replace("%arena_id%", name);
                    plugin.getUtils().sendMessage(player,errorMsg);
                    return;
                }
                List<String> signs = plugin.getFiles().getControl(Files.GAMES).getStringList("games." + name + ".signs");
                signs.add(plugin.getUtils().getStringFromLocation(event.getBlock().getLocation()));
                plugin.getFiles().getControl(Files.GAMES).set("games." + name + ".signs",signs);
                plugin.getFiles().save(SaveMode.GAMES_FILES);
                game.loadSigns();
            }
        }catch (Throwable throwable) {
            plugin.getLogs().error("Can't create plugin sign");
            plugin.getLogs().error(throwable);
        }
    }
    @EventHandler
    public void SignInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Player player = e.getPlayer();
        try {
            if (e.getClickedBlock().getState() instanceof Sign) {
                for (Game game : plugin.getGameManager().getGames()) {
                    if (game.signs.contains(e.getClickedBlock().getLocation())) {
                        game.join(player);
                        return;
                    }
                }
            }
        }catch (Throwable ignored) {}
    }
    @EventHandler
    public void joinTeleport(PlayerJoinEvent event) {
        try {
            FileConfiguration file = plugin.getFiles().getControl(Files.SETTINGS);
            if (file.getBoolean("settings.options.joinLobbyTeleport")) {
                if (file.getString("settings.lobbyLocation").equalsIgnoreCase("notSet")) {
                    plugin.getLogs().error("-----------------------------");
                    plugin.getLogs().error("Can't teleport player to lobby location, lobby location is not set");
                    plugin.getLogs().error("-----------------------------");
                } else {
                    String[] loc = file.getString("settings.lobbyLocation").split(",");
                    World w = Bukkit.getWorld(loc[0]);
                    double x = Double.parseDouble(loc[1]);
                    double y = Double.parseDouble(loc[2]);
                    double z = Double.parseDouble(loc[3]);
                    float yaw = Float.parseFloat(loc[4]);
                    float pitch = Float.parseFloat(loc[5]);
                    Location location = new Location(w, x, y, z, yaw, pitch);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,() -> {
                        try {
                            event.getPlayer().teleport(location);
                        } catch (Exception ex) {
                            plugin.getLogs().error("Can't teleport player to lobby on join");
                        }
                    });
                    if(plugin.getFiles().getControl(Files.SCOREBOARD).getBoolean("scoreboards.lobby.toggle")) {
                        plugin.getScoreboards().setScoreboard(RigoxBoard.LOBBY,event.getPlayer());
                    }
                    if(file.getBoolean("settings.options.joinHeal")) {
                        event.getPlayer().setHealth(20.0D);
                        event.getPlayer().setLevel(0);
                        event.getPlayer().setFoodLevel(20);
                        event.getPlayer().setExp(0.0F);
                    }
                    if(file.getBoolean("settings.options.lobby-actionBar")) {
                        plugin.getUtils().sendActionbar(event.getPlayer(),plugin.getFiles().getControl(Files.MESSAGES).getString("messages.lobby.actionBar"));
                    }
                    if(file.getBoolean("settings.options.joinAdventureGamemode")) {
                        event.getPlayer().setGameMode(GameMode.ADVENTURE);
                    }
                }
            }
        } catch (Throwable throwable) {
            plugin.getLogs().error("Can't teleport " + event.getPlayer().getName() +" to the lobby!");
            plugin.getLogs().error(throwable);
        }
    }
}