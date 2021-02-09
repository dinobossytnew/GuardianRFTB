package dev.mruniverse.rigoxrftb.core.api;

import dev.mruniverse.rigoxrftb.core.games.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameStartEvent extends Event {
    private final Game game;
    private static final HandlerList handlerList = new HandlerList();

    public GameStartEvent(Game rftbGame) {
        game = rftbGame;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
    public Game getCurrentGame() {
        return game;
    }
}