package dev.mruniverse.rigoxrftb.core.games;

import dev.mruniverse.rigoxrftb.core.RigoxRFTB;
import dev.mruniverse.rigoxrftb.core.enums.Files;
import dev.mruniverse.rigoxrftb.core.enums.SaveMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Objects;

public class GameManager {
    private ArrayList<Game> games = new ArrayList<>();
    private final RigoxRFTB plugin;
    public GameManager(RigoxRFTB main) {
        plugin = main;
    }
    public Game getGame(String gameName) {
        if (this.games.size() < 1)
            return null;
        for (Game game : this.games) {
            if (game.getName().equalsIgnoreCase(gameName))
                return game;
        }
        return null;
    }

    public Game getGame(Player player) {
        return plugin.getPlayerData(player.getUniqueId()).getGame();
    }

    public boolean existGame(String name) {
        return (getGame(name) != null);
    }

    public boolean isPlaying(Player player) {
        return (getGame(player) != null);
    }

    public void joinGame(Player player,String gameName) {
        if(!existGame(gameName)) {
            plugin.getUtils().sendMessage(player, Objects.requireNonNull(plugin.getFiles().getControl(Files.MESSAGES).getString("messages.admin.arenaError")).replace("%arena_id%",gameName));
        }
        Game game = getGame(gameName);
        game.join(player);
    }
    public void createGameFiles(String gameName) {
        FileConfiguration gameFiles = plugin.getFiles().getControl(Files.GAMES);
        gameFiles.set("games." + gameName + ".time", 500);
        gameFiles.set("games." + gameName + ".max", 10);
        gameFiles.set("games." + gameName + ".min", 2);
        gameFiles.set("games." + gameName + ".worldTime", 0);
        gameFiles.set("games." + gameName + ".locations.waiting", "notSet");
        gameFiles.set("games." + gameName + ".locations.selected-beast", "notSet");
        gameFiles.set("games." + gameName + ".locations.beast", "notSet");
        gameFiles.set("games." + gameName + ".locations.runners", "notSet");
        plugin.getFiles().save(SaveMode.GAMES_FILES);
    }
    public void setWaiting(String gameName, Location location) {
        try {
            String gameLoc = location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
            plugin.getFiles().getControl(Files.GAMES).set("games." + gameName + ".locations.waiting", gameLoc);
            plugin.getFiles().save(SaveMode.GAMES_FILES);
        }catch (Throwable throwable) {
            plugin.getLogs().error("Can't set waiting lobby for game: " + gameName);
            plugin.getLogs().error(throwable);
        }
    }
    public void setSelectedBeast(String gameName, Location location) {
        try {
            String gameLoc = location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
            plugin.getFiles().getControl(Files.GAMES).set("games." + gameName + ".locations.selected-beast", gameLoc);
            plugin.getFiles().save(SaveMode.GAMES_FILES);
        }catch (Throwable throwable) {
            plugin.getLogs().error("Can't set selected beast location for game: " + gameName);
            plugin.getLogs().error(throwable);
        }
    }
    public void setBeast(String gameName, Location location) {
        try {
            String gameLoc = location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
            plugin.getFiles().getControl(Files.GAMES).set("games." + gameName + ".locations.beast", gameLoc);
            plugin.getFiles().save(SaveMode.GAMES_FILES);
        }catch (Throwable throwable) {
            plugin.getLogs().error("Can't set beast spawn location for game: " + gameName);
            plugin.getLogs().error(throwable);
        }
    }
    public void setRunners(String gameName, Location location) {
        try {
            String gameLoc = location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
            plugin.getFiles().getControl(Files.GAMES).set("games." + gameName + ".locations.runners", gameLoc);
            plugin.getFiles().save(SaveMode.GAMES_FILES);
        }catch (Throwable throwable) {
            plugin.getLogs().error("Can't set runners spawn location for game: " + gameName);
            plugin.getLogs().error(throwable);
        }
    }
    public void setMax(String gameName,Integer max) {
        plugin.getFiles().getControl(Files.GAMES).set("games." + gameName + ".max", max);
        plugin.getFiles().save(SaveMode.GAMES_FILES);
    }
    public void setMin(String gameName,Integer min) {
        plugin.getFiles().getControl(Files.GAMES).set("games." + gameName + ".min", min);
        plugin.getFiles().save(SaveMode.GAMES_FILES);
    }






}
