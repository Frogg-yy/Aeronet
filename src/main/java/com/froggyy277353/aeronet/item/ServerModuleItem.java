package com.froggyy277353.aeronet.item;

import com.froggyy277353.aeronet.block.entity.server.ServerModuleType;
import net.minecraft.world.item.Item;

public class ServerModuleItem extends Item {
    private final ServerModuleType type;
    public ServerModuleItem(ServerModuleType type, Properties properties)
    {
        super(properties);
        this.type = type;
    }
    public ServerModuleType getType()
    {
        return type;
    }
}
