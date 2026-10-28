package git.artdeell.mojo.game.render;

public class FpsCounter {
    private static int frames = 0;
    private static long windowStart = System.nanoTime();
    private static int lastFps = 0;

    public static synchronized void onFrame() {
        frames++;
        long now = System.nanoTime();
        if (now - windowStart >= 1_000_000_000L) {
            lastFps = frames;
            frames = 0;
            windowStart = now;
        }
    }

    public static synchronized int getFps() {
        // Si hace más de 2 segundos que no llegan cuadros, muestra 0
        if (System.nanoTime() - windowStart >= 2_000_000_000L) {
            lastFps = 0;
        }
        return lastFps;
    }
  }
