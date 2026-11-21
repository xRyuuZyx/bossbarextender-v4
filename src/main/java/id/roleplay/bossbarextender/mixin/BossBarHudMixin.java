package id.roleplay.bossbarextender.mixin;

import id.roleplay.bossbarextender.BossBarExtenderConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.UUID;

/**
 * Extends vanilla's top-third cutoff and maps both the bar and its title to a
 * compact row position when the current window is shorter. Vanilla still owns
 * the render loop and the original render methods, preserving resource-pack
 * fonts/textures and compatibility with normal render hooks.
 */
@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {
    private static final int VANILLA_TOP_OFFSET = 12;
    private static final int VANILLA_ROW_STEP = 19;
    private static final int BOTTOM_PADDING = 5;
    private static final int MIN_ROW_STEP = 14;

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private Map<UUID, ?> bossBars;

    @Shadow
    private void renderBossBar(DrawContext context, int x, int y, BossBar bossBar) {
        throw new AssertionError("Shadow method was not transformed");
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowHeight()I"
            )
    )
    private int bossbarExtender$expandVanillaRenderHeight(DrawContext context) {
        int actualHeight = context.getScaledWindowHeight();
        int percent = BossBarExtenderConfig.get().maxScreenUsagePercent();

        // Vanilla's loop still advances its internal row index by 19px. Give
        // that loop enough virtual height to visit every active BossBar; the
        // two redirects below then move each bar/title into the compact layout
        // that fits the real screen.
        int visibleHeight = actualHeight * percent / 100;
        int requiredLoopHeight = VANILLA_TOP_OFFSET
                + Math.max(0, this.bossBars.size() - 1) * VANILLA_ROW_STEP + 1;
        int loopHeight = Math.max(visibleHeight, requiredLoopHeight);
        return loopHeight * 3;
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/BossBarHud;renderBossBar(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/entity/boss/BossBar;)V"
            )
    )
    private void bossbarExtender$renderBarAtFittedY(
            BossBarHud instance, DrawContext context, int x, int originalY, BossBar bossBar) {
        int rowIndex = Math.max(0, (originalY - VANILLA_TOP_OFFSET) / VANILLA_ROW_STEP);
        int fittedY = VANILLA_TOP_OFFSET + rowIndex * this.fittedRowStep();
        this.renderBossBar(context, x, fittedY, bossBar);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"
            )
    )
    private void bossbarExtender$renderTitleAtFittedY(
            DrawContext context, TextRenderer textRenderer, Text text,
            int centerX, int originalY, int color) {
        int originalBarY = originalY + 9;
        int rowIndex = Math.max(0,
                (originalBarY - VANILLA_TOP_OFFSET) / VANILLA_ROW_STEP);
        int fittedY = VANILLA_TOP_OFFSET + rowIndex * this.fittedRowStep() - 9;
        context.drawCenteredTextWithShadow(textRenderer, text, centerX, fittedY, color);
    }

    private int fittedRowStep() {
        int count = this.bossBars.size();
        if (count <= 1) {
            return VANILLA_ROW_STEP;
        }

        int screenHeight = this.client.getWindow().getScaledHeight();
        int percent = BossBarExtenderConfig.get().maxScreenUsagePercent();
        int bottom = screenHeight * percent / 100;
        int usableHeight = Math.max(1,
                bottom - VANILLA_TOP_OFFSET - BOTTOM_PADDING);
        int fit = usableHeight / (count - 1);

        // The title line is 9px high and the vanilla bar is 5px high, so
        // 14px is the smallest row step that avoids them colliding.
        return Math.max(MIN_ROW_STEP, Math.min(VANILLA_ROW_STEP, fit));
    }
}
