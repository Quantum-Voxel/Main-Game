package dev.ultreon.qvoxel.client;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.gui.Overlay;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.shader.Reloadable;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.resource.ReloadContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

public class LoadingOverlay extends Overlay {
    private final SequencedMap<String, ReloadHandler> processes = new LinkedHashMap<>();
    private final Set<String> finished = new CopyOnWriteArraySet<>();
    private boolean opened;
    private final QuantumClient client;
    private ReloadContext context;
    private final List<String> messages = new ArrayList<>();
    private CompletableFuture<?> reloadFuture;

    public LoadingOverlay(QuantumClient client) {
        this.client = client;
    }

    public void registerReloadProcess(String name, Reloadable reloadable, String... deps) {
        processes.put(name, new ReloadHandler(name, reloadable, deps));
    }

    public void open() {
        opened = true;
    }

    public void finish() {
        reloadFuture.join();
        reloadFuture = null;
        finished.clear();
        synchronized (messages) {
            messages.clear();
        }
    }

    public void close() {
        opened = false;
    }

    @Override
    public void render(ClientPlayerEntity player, GuiRenderer renderer, float partialTick) {
        if (!opened) {
            return;
        }

        width = client.getScaledWidth();
        height = client.getScaledHeight();

        renderer.pushMatrix();
        renderer.drawCenteredString("Quantum Voxel", width / 16, height / 16, 0xfffffff);
        renderer.popMatrix();
        renderer.fillRect(0, 0, width, height, 0xffff7020);
        renderer.fillRect(0, height - 4, width, 4, 0x80ffffff);
        renderer.fillRect(0, height - 4, (int) (width * getLoadingRatio()), 4, 0x80ffffff);

        int y = height - 10;
        synchronized (messages) {
            for (int i = messages.size() - 1; i >= 0; i--) {
                renderer.drawString(messages.get(i), 20, y - 20, 0xffffffff, false);
                y -= 11;
            }
        }
    }

    private float getLoadingRatio() {
        return (float) getDone() / getAllProcessCount();
    }

    private int getAllProcessCount() {
        return processes.size();
    }

    private int getDone() {
        return finished.size();
    }

    public void reload(ReloadContext context) {
        reloadFuture = new CompletableFuture<>();
        this.context = context;
        context.setMessageHandler(e -> {
            synchronized (messages) {
                messages.add(e);
                if (messages.size() > 100) {
                    messages.removeFirst();
                }
            }
        });
        for (ReloadHandler value : processes.values()) {
            value.start();
        }
        for (ReloadHandler value : processes.values()) {
            value.addWaitBarrier(processes::get);
            value.reload(context).thenRun(() -> {
                finish(value);
            }).exceptionally(throwable -> {
                CommonConstants.LOGGER.error("Reload handler {} failed!", value.name(), throwable);
                finish(value);
                return null;
            });
        }
    }

    private void finish(ReloadHandler value) {
        finished.add(value.name());
        if (finished.size() == processes.size()) {
            reloadFuture.complete(null);
        }
    }

    public boolean isOpen() {
        return opened;
    }

    /*

        textureManager.reload(context);
        modelManager.reload(context);

        shaders.reload(context);
        RenderType.reloadAll(context);

        BlockRenderTypeRegistry.reload(context);
     */
}
