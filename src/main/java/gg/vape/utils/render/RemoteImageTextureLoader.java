package gg.vape.utils.render;

public class RemoteImageTextureLoader {
    private static String legacyMarker;

    public static void setLegacyMarker(String legacyMarker) {
        RemoteImageTextureLoader.legacyMarker = legacyMarker;
    }

    public static String getLegacyMarker() {
        return legacyMarker;
    }

    // 纯离线：不再请求 minotar.net，头像走 default_user 占位
    public static byte[] download(String urlString) {
        return null;
    }

    private static Throwable identityThrowable(Throwable throwable) {
        return throwable;
    }

    static {
        RemoteImageTextureLoader.setLegacyMarker("Gazrnb");
    }
}
