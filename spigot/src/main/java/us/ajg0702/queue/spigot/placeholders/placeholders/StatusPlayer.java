package us.ajg0702.queue.spigot.placeholders.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.ajg0702.queue.api.spigot.AjQueueSpigotAPI;
import us.ajg0702.queue.api.spigot.MessagedResponse;
import us.ajg0702.queue.spigot.SpigotMain;
import us.ajg0702.queue.spigot.placeholders.Placeholder;
import us.ajg0702.queue.spigot.utils.UUIDStringKey;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;

public class StatusPlayer extends Placeholder {
    public StatusPlayer(SpigotMain plugin) {
        super(plugin);
    }

    private final Map<UUIDStringKey, String> cache = new ConcurrentHashMap<>();

    @Override
    public String getRegex() {
        return "status_(.*)_player";
    }

    @Override
    public String parse(Matcher matcher, OfflinePlayer p) {
        String queue = matcher.group(1);
        UUIDStringKey key = new UUIDStringKey(p.getUniqueId(), queue);

        if(!p.isOnline()) return "You aren't online!";

        plugin.getScheduler().runTaskAsynchronously(() -> {
            if(!p.isOnline()) return;
            try {
                String response = AjQueueSpigotAPI.getInstance()
                        .getServerStatusString(queue, p.getUniqueId())
                        .get(30, TimeUnit.SECONDS);

                cache.put(key, response);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException | IllegalArgumentException ignored) {}
        });

        return cache.getOrDefault(key, "...");
    }

    @Override
    public void cleanCache(Player player) {
        cache.entrySet().removeIf(entry -> entry.getKey().getUuid().equals(player.getUniqueId()));
    }
}
