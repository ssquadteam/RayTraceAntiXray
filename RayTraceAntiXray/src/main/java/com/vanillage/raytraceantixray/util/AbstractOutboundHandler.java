package com.vanillage.raytraceantixray.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.NoSuchElementException;
import java.util.Objects;

public class AbstractOutboundHandler extends ChannelOutboundHandlerAdapter {

    private final String name;

    private Connection connection;
    private Channel channel;

    public AbstractOutboundHandler(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getAttachedName() {
        return name;
    }

    public Channel getAttachedChannel() {
        return channel;
    }

    public Connection getAttachedConnection() {
        return connection;
    }

    public boolean attach(Player player) {
        return attach(player.getAddress().getAddress());
    }

    public boolean attach(InetAddress address) {
        var connection = NetworkUtil.getServerConnection(address);
        if (connection == null) return false;
        detach();
        detach(address, name);
        Channel channel = Objects.requireNonNull(connection.channel, "Channel is null");
        ChannelPipeline pipe = channel.pipeline();
        if (pipe.get("packet_handler") == null) {
            pipe.addLast(name, this);
        } else {
            pipe.addBefore("packet_handler", name, this);
        }
        this.connection = connection;
        this.channel = channel;
        return true;
    }

    public void detach() {
        if (channel != null) {
            detach(channel, name);
            channel = null;
            connection = null;
        }
    }

    public static boolean detach(Player player, String name) {
        return detach(player.getAddress().getAddress(), name);
    }

    public static boolean detach(InetAddress address, String name) {
        var connection = NetworkUtil.getServerConnection(address);
        if (connection != null) return detach(Objects.requireNonNull(connection.channel, "Channel is null"), name);
        return false;
    }

    private static boolean detach(Channel channel, String name) {
        try {
            channel.pipeline().remove(name);
            return true;
        } catch (NoSuchElementException ignored) {}
        return false;
    }

}
