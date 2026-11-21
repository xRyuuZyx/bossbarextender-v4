package id.roleplay.bossbarextender;

import net.fabricmc.api.ClientModInitializer;

public final class BossBarExtenderClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BossBarExtenderConfig.load();
        // Screen-size sync is handled by the separate uistudio_screen_sync mod.
        // Do not register uistudio:screen here as that payload is already
        // registered by the helper mod. Registering it twice crashes startup.
    }
}
