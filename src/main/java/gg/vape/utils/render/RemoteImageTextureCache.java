package gg.vape.utils.render;

import gg.vape.Vape;
import gg.vape.unmap.ImageParser$Format;
import gg.vape.utils.render.GlImageTexture;
import gg.vape.utils.render.ImageRenderer;
import gg.vape.utils.render.RemoteImageTextureLoader;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

class RemoteImageTextureCache {
    private static final byte[] UNAVAILABLE_IMAGE = new byte[0];
    private final int imageSize;
    private ConcurrentLinkedQueue<String> pendingUsernames = new ConcurrentLinkedQueue();
    private ConcurrentHashMap<String, GlImageTexture> textures;
    private ConcurrentHashMap<String, byte[]> downloadedImages = new ConcurrentHashMap();

    void processPendingDownloads() {
        while (!this.pendingUsernames.isEmpty()) {
            this.download(this.pendingUsernames.poll());
        }
    }

    public RemoteImageTextureCache(int imageSize) {
        this.textures = new ConcurrentHashMap();
        this.imageSize = imageSize;
    }

    byte[] getDownloadedImage(String username) {
        if (this.downloadedImages.containsKey(username)) {
            byte[] imageData = this.downloadedImages.get(username);
            // 空数组是"离线/获取失败"占位标记，对调用方一律表现为 null，避免每帧重复入队重试
            return imageData == UNAVAILABLE_IMAGE ? null : imageData;
        }
        if (!this.pendingUsernames.contains(username)) {
            this.pendingUsernames.add(username);
        }
        return null;
    }

    private static Exception identityException(Exception exception) {
        return exception;
    }

    GlImageTexture getTexture(String username) {
        if (this.textures.containsKey(username)) {
            return this.textures.get(username);
        }
        byte[] imageData = this.getDownloadedImage(username);
        if (imageData == null) {
            return null;
        }
        GlImageTexture texture = null;
        try {
            texture = new GlImageTexture(new ByteArrayInputStream(imageData), 9729, ImageParser$Format.RGBA);
        }
        catch (Exception exception) {
            Vape.logThrowable(exception);
            texture = ImageRenderer.loadResource("default_user", false, false);
        }
        this.textures.put(username, texture);
        return texture;
    }

    void download(String username) {
        byte[] imageData = RemoteImageTextureLoader.download("https://minotar.net/avatar/" + username + "/" + this.imageSize + ".png");
        // 纯离线：RemoteImageTextureLoader.download 恒返回 null（不再请求 minotar.net），
        // ConcurrentHashMap 不接受 null 值，这里写入占位标记，头像由 default_user 占位图兜底
        this.downloadedImages.put(username, imageData == null ? UNAVAILABLE_IMAGE : imageData);
    }
}
