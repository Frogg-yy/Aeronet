package com.froggyy277353.aeronet.network;

import com.froggyy277353.aeronet.block.entity.server.ServerBlockEntity;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkManager {
    private static final NetworkManager INSTANCE = new NetworkManager();
    private final Set<ServerBlockEntity> servers = new HashSet<>();

    private NetworkManager(){}
    public static NetworkManager getInstance()
    {
        return INSTANCE;
    }

    public synchronized boolean register(ServerBlockEntity server)
    {
        servers.removeIf(existing -> existing.isRemoved() || existing.getLevel() == null);
        if (server.getLevel() == null) return false;
        String name = server.getNetWorkName();
        if (name == null || name.isBlank()) return false;
        if (servers.stream().anyMatch(existing -> existing != server
                && existing.getLevel().isClientSide == server.getLevel().isClientSide
                && name.equals(existing.getNetWorkName()))) return false;
        servers.add(server);
        return true;
    }
    public synchronized Set<ServerBlockEntity> getServers()
    {
        return Set.copyOf(servers);
    }

    public synchronized ServerBlockEntity findServer(ServerBlockEntity owner, String name, String password) {
        if (name.isEmpty() || !(owner.getLevel() instanceof net.minecraft.server.level.ServerLevel ownLevel))
            return null;
        for (ServerBlockEntity server : servers) {
            if (server != owner && !server.isRemoved() && server.isConnected()
                    && server.getLevel() instanceof net.minecraft.server.level.ServerLevel level
                    && level.getServer() == ownLevel.getServer()
                    && name.equals(server.getNetWorkName()) && password.equals(server.getNetWorkPassword()))
                return server;
        }
        return null;
    }
    public boolean checkServerInList(ServerBlockEntity server)
    {
        return servers.contains(server);
    }
    public synchronized boolean unregister(ServerBlockEntity server)
    {
        if (servers.contains(server))
        {
            servers.remove(server);
            return true;
        }
        else
        {
            return false;
        }
    }
    public List<ServerBlockEntity> getServersInRadius(BlockPos center, double radius)
    {
        List<ServerBlockEntity> result = new ArrayList<>();

        double radiusSq = radius * radius;

        for (ServerBlockEntity server : servers)
        {
            if (server.getLevel() != null && server.getLevel().isClientSide
                    && server.isConnected()
                    && server.getBlockPos().distSqr(center) <= radiusSq)
            {
                result.add(server);
            }
        }
        return result;
    }

}
