package git.artdeell.mojo.game.render;

import static git.artdeell.mojo.CallbackBridge.windowHeight;
import static git.artdeell.mojo.CallbackBridge.windowWidth;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import git.artdeell.mojo.Tools;
import git.artdeell.mojo.prefs.LauncherPreferences;

public class TextureViewSurfaceProvider implements SurfaceProvider {
    private TextureView mTextureView;
    private SurfaceCallback mCallback;
    private TextView mFpsView;

    @Override
    public View create(Context context, SurfaceCallback callback) {
        mCallback = callback;
        mTextureView = new TextureView(context);
        mTextureView.setOpaque(true);
        mTextureView.setAlpha(1.0f);
        mTextureView.setSurfaceTextureListener(new CallbackAdapter());
        addFpsOverlay(context);
        return mTextureView;
    }

    private void addFpsOverlay(Context context) {
        if (mFpsView != null) return;
        if (LauncherPreferences.DEFAULT_PREF != null
        && LauncherPreferences.DEFAULT_PREF.getBoolean("hideFps", false)) return;
        Context base = context;
        while (base instanceof ContextWrapper && !(base instanceof Activity)) {
            base = ((ContextWrapper) base).getBaseContext();
        }
        if (!(base instanceof Activity)) return;

        final TextView fpsView = new TextView(base);
        fpsView.setTextColor(Color.WHITE);
        fpsView.setTextSize(12);
        fpsView.setBackgroundColor(0x88000000);
        fpsView.setPadding(12, 6, 12, 6);
        fpsView.setText("Pantalla: 0");
        mFpsView = fpsView;

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable updater = new Runnable() {
            @Override
            public void run() {
                fpsView.setText("Pantalla: " + FpsCounter.getFps());
                handler.postDelayed(this, 500);
            }
        };
        fpsView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                handler.post(updater);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                handler.removeCallbacks(updater);
            }
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        String corner = "top_center";
if (LauncherPreferences.DEFAULT_PREF != null) {
    corner = LauncherPreferences.DEFAULT_PREF.getString("fpsCorner", "top_center");
}
if ("top_left".equals(corner)) {
    params.gravity = Gravity.TOP | Gravity.START;
} else if ("top_right".equals(corner)) {
    params.gravity = Gravity.TOP | Gravity.END;
} else {
    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
}
        ((Activity) base).addContentView(fpsView, params);
    }

    @Override
    public void updateSize() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        if(surfaceTexture != null) {
            surfaceTexture.setDefaultBufferSize(windowWidth, windowHeight);
            Tools.runOnUiThread(()->mCallback.onSurfaceResized());
        }
    }

    private class CallbackAdapter implements TextureView.SurfaceTextureListener {

        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            if(windowWidth != 0 && windowHeight != 0)
                surfaceTexture.setDefaultBufferSize(windowWidth, windowHeight);
            mCallback.onSurfaceAvailable(new Surface(surfaceTexture));
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            mCallback.onSurfaceDestroyed();
            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            mCallback.onSurfaceResized();
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
            FpsCounter.onFrame();
        }
    }
            }
