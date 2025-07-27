package us.ajg0702.queue.platforms.bungeecord;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import us.ajg0702.queue.api.PlatformMethods;
import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.util.QueueLogger;
import us.ajg0702.queue.commands.commands.PlayerSender;
import us.ajg0702.queue.platforms.bungeecord.players.BungeePlayer;
import us.ajg0702.queue.platforms.bungeecord.server.BungeeServer;

import java.util.*;

public class BungeeMethods implements PlatformMethods {

    final ProxyServer proxyServer;
    final QueueLogger logger;
    final BungeeQueue plugin;

    public BungeeMethods(BungeeQueue plugin, ProxyServer proxyServer, QueueLogger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.plugin = plugin;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void sendPluginMessage(AdaptedPlayer player, String channel, String... data) {
        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
        if (networkPlayers != null && !networkPlayers.isEmpty()) {
            String playerName = player.getName();
            if(playerName == null) return;
            if(plugin.getMain().getConfig().getBoolean("disable-proxy-communication")) return;
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(channel);

            for (String s : data) {
                out.writeUTF(s);
            }
            ProxiedPlayer proxiedPlayer = ((BungeePlayer) player).getHandle();
            if(proxiedPlayer == null) return;
            Server server = proxiedPlayer.getServer();
            if(server == null) return;
            server.sendData("ajqueue:tospigot", out.toByteArray());
        }
    }

    @Override
    public AdaptedPlayer senderToPlayer(ICommandSender sender) {
        if(sender instanceof PlayerSender) {
            return ((PlayerSender) sender).getHandle();
        }
        return new BungeePlayer((ProxiedPlayer) sender.getHandle());
    }

    @Override
    public String getPluginVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public List<AdaptedPlayer> getOnlinePlayers() {
        List<AdaptedPlayer> players = new ArrayList<>();
        proxyServer.getPlayers().forEach(pp -> players.add(new BungeePlayer(pp)));
        return players;
    }

    @Override
    public List<String> getPlayerNames(boolean lowercase) {
        List<String> names = new ArrayList<>();
        proxyServer.getPlayers().forEach(player -> names.add(lowercase ? player.getName().toLowerCase(Locale.ROOT) : player.getName()));
        return names;
    }

    @Override
    public AdaptedPlayer getPlayer(String name) {
        ProxiedPlayer player = proxyServer.getPlayer(name);
        if(player == null) return null;
        return new BungeePlayer(player);
    }

    @Override
    public AdaptedPlayer getPlayer(UUID uuid) {
        ProxiedPlayer player = proxyServer.getPlayer(uuid);
        if(player == null) return null;
        return new BungeePlayer(player);
    }

    @Override
    public List<String> getServerNames() {
        return new ArrayList<>(proxyServer.getServers().keySet());
    }

    @Override
    public String getImplementationName() {
        return "BungeeCord";
    }

    @Override
    public List<IBaseCommand> getCommands() {
        return plugin.commands;
    }

    @Override
    public boolean hasPlugin(String pluginName) {
        return proxyServer.getPluginManager().getPlugin(pluginName) != null;
    }

    @Override
    public AdaptedServer getServer(String name) {
        List<? extends AdaptedServer> servers = getServers();
        for (AdaptedServer server : servers) {
            if(server.getName().equals(name)) return server;
        }
        return null;
    }

    List<BungeeServer> serverList = new ArrayList<>();

    @Override
    public List<? extends AdaptedServer> getServers() {
        for (ServerInfo serverInfo : proxyServer.getServers().values()) {
            boolean found = false;
            for(BungeeServer sv : new ArrayList<>(serverList)) {
                if(sv.getHandle().equals(serverInfo) && sv.getName().equals(serverInfo.getName())) {
                    found = true;
                    break;
                }
            }
            if(found) continue;

            serverList.add(new BungeeServer(serverInfo));
        }

        for(BungeeServer sv : new ArrayList<>(serverList)) {
            boolean found = false;
            for (ServerInfo serverInfo : proxyServer.getServers().values()) {
                if(sv.getHandle().equals(serverInfo) && sv.getName().equals(serverInfo.getName())) {
                    found = true;
                    break;
                }
            }
            if(found) continue;

            serverList.remove(sv);
        }

        return serverList;
    }

    @Override
    public String getProtocolName(int protocol) {
        return protocol+"";
    }
}
