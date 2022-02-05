package net.splatcraft.cca;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.splatcraft.component.PlayerDataComponent;
import net.splatcraft.component.SplatcraftComponents;

public class SplatcraftCardinalComponents implements EntityComponentInitializer {
    private static SplatcraftCardinalComponents instance = null;

    public SplatcraftCardinalComponents() {
        instance = this;
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(SplatcraftComponents.PLAYER_DATA, PlayerDataComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }

    public static SplatcraftCardinalComponents getInstance() {
        return instance;
    }
}
