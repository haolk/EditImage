package com.lafonapps.common.ad.adapter.banner;

import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lafonapps.common.ad.AdSize;
import com.lafonapps.common.ad.adapter.AdAdapterLayout;
import com.lafonapps.common.ad.adapter.AdModel;
import com.lafonapps.common.ad.adapter.BannerViewAdapter;
import com.lafonapps.common.preferences.CommonConfig;
import com.oppo.mobad.api.ad.BannerAd;
import com.oppo.mobad.api.listener.IBannerAdListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjie on 2017/7/5.
 */

public class BannerAdapterView extends AdAdapterLayout implements BannerViewAdapter {

    private static final String TAG = BannerAdapterView.class.getCanonicalName();

    public static final boolean REUSEABLE = false;

    private BannerAd bannerAd;
    private Activity activity;
    private AdModel adModel;
    private String[] debugDevices;
    private boolean ready;
    private List<Listener> allListeners = new ArrayList<>();

    public BannerAdapterView(Activity activity) {
        super(activity);
        this.activity = activity;

        this.setTouchListener(new TouchListener() {
            @Override
            public boolean shouldComfirmBeforeDownloadApp() {
                return CommonConfig.sharedCommonConfig.shouldComfirmBeforeDownloadAppOnBannerViewClick;
            }

            @Override
            public Rect exceptRect() {
                return new Rect(0, 0, 18, 18);
            }
        });
    }

    @Override
    public void setDebugDevices(String[] debugDevices) {
        this.debugDevices = debugDevices;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    @Override
    public void build(AdModel adModel, AdSize adSize) {
        this.adModel = adModel;
        bannerAd = new BannerAd(activity, adModel.getOppoAdID());
        bannerAd.setAdListener(new IBannerAdListener() {
            @Override
            public void onAdClose() {
                Log.d(TAG, "onAdClosed");

                Listener[] listeners = getAllListeners();
                for (Listener listener : listeners) {
                    listener.onAdClosed(BannerAdapterView.this);
                }
            }

            @Override
            public void onAdShow() {
                Log.d(TAG, "onAdLeftApplication");
                Listener[] listeners = getAllListeners();
                for (Listener listener : listeners) {
                    listener.onAdLeftApplication(BannerAdapterView.this);
                }
            }

            @Override
            public void onAdFailed(String s) {
                Log.d(TAG, "onAdFailedToLoad:" + s);

                Listener[] listeners = getAllListeners();
                for (Listener listener : listeners) {
                    listener.onAdFailedToLoad(BannerAdapterView.this, -1);
                }
            }

            @Override
            public void onAdReady() {
                Log.d(TAG, "onAdLoaded");
                ready = true;
                Listener[] listeners = getAllListeners();
                for (Listener listener : listeners) {
                    listener.onAdLoaded(BannerAdapterView.this);
                }
            }

            @Override
            public void onAdClick() {
                Log.d(TAG, "onAdOpened");

                resetComfirmed();

                Listener[] listeners = getAllListeners();
                for (Listener listener : listeners) {
                    listener.onAdOpened(BannerAdapterView.this);
                }

                //点击后刷新广告
                loadAd();
            }
        });

        removeAllViews();

        View adView = bannerAd.getAdView();
        if (null != adView) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            this.addView(adView, layoutParams);
        } else {
            Log.w(TAG, "bannerAd.getAdView() return null");
        }
    }

    @Override
    public void loadAd() {
        bannerAd.loadAd();
    }

    @Override
    public View getAdapterAdView() {
        return this;
    }

    /* SupportMutableListenerAdapter */

    @Override
    public synchronized void addListener(Listener listener) {
        if (listener != null && !allListeners.contains(listener)) {
            allListeners.add(listener);
            Log.d(TAG, "addListener:" + listener);
        }
    }

    @Override
    public synchronized void removeListener(Listener listener) {
        if (allListeners.contains(listener)) {
            allListeners.remove(listener);
            Log.d(TAG, "removeListener:" + listener);
        }
    }

    @Override
    public Listener[] getAllListeners() {
        return allListeners.toArray(new Listener[allListeners.size()]);
    }
}
