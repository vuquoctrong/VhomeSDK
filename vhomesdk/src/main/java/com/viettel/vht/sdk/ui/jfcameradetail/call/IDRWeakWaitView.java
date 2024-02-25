package com.viettel.vht.sdk.ui.jfcameradetail.call;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lib.FunSDK;
import com.viettel.vht.sdk.R;

import java.lang.ref.WeakReference;

/**
 * Created by SJ on 2017-10-26.
 */

public class IDRWeakWaitView {
    private ImageView mImgClock;
    private ImageView mImgSZ;
    private ImageView mImgMZ;
    private ImageView mImgBZ;
    private ImageView mImgLeftB;
    private ImageView mImgLeftS;
    private ImageView mImgRightS;
    private ImageView mImgRightB;
    private ViewGroup mViewGroup;
    private TextView mTxtPrompt;
    private Animator[] mWeakAnims;
    private Animator[] mConnectAnims;
    private WeakReference<View> mRootView;
    private boolean isShow;
    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            startConnectAnim();
        }
    };

    public IDRWeakWaitView(Activity activity) {
        mRootView = new WeakReference<>(activity.getWindow().getDecorView());

        mViewGroup = (ViewGroup) LayoutInflater.from(activity)
                .inflate(R.layout.view_idr_wait_weak, null);
        mViewGroup.setLayoutParams(new ViewGroup.LayoutParams(
                Resources.getSystem().getDisplayMetrics().widthPixels,
                Resources.getSystem().getDisplayMetrics().heightPixels));
        mViewGroup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        initView();
    }

    private void initView() {
        mTxtPrompt = mViewGroup.findViewById(R.id.txtPrompt);
        mImgClock = mViewGroup.findViewById(R.id.imgClock);
        mImgSZ = mViewGroup.findViewById(R.id.imgSZ);
        mImgMZ = mViewGroup.findViewById(R.id.imgMZ);
        mImgBZ = mViewGroup.findViewById(R.id.imgBZ);
        mImgLeftS = mViewGroup.findViewById(R.id.imgLeftS);
        mImgLeftB = mViewGroup.findViewById(R.id.imgLeftB);
        mImgRightS = mViewGroup.findViewById(R.id.imgRightS);
        mImgRightB = mViewGroup.findViewById(R.id.imgRightB);
    }

    public void show() {
        if (isShow || mRootView == null || mRootView.get() == null) {
            return;
        }
        isShow = true;
        ViewGroup viewGroup = (ViewGroup) this.mRootView.get();
        viewGroup.addView(mViewGroup);
        startWeakAnim();
    }

    private void stopWeakAnim() {
        if (mWeakAnims != null) {
            mWeakAnims[0].cancel();
            mWeakAnims[1].cancel();
            mWeakAnims[2].cancel();
        }
        mImgSZ.setVisibility(View.INVISIBLE);
        mImgMZ.setVisibility(View.INVISIBLE);
        mImgBZ.setVisibility(View.INVISIBLE);
    }

    private void startWeakAnim() {
        stopConnectAnim();
        mTxtPrompt.setText(FunSDK.TS("Waking_up"));
        if (mWeakAnims == null) {
            mWeakAnims = new Animator[3];
            ObjectAnimator sAlphaAnim = ObjectAnimator.ofFloat(mImgSZ,"alpha", 1f, 0f);
            sAlphaAnim.setDuration(1500);
            sAlphaAnim.setRepeatCount(ObjectAnimator.INFINITE);
            mWeakAnims[0] = sAlphaAnim;
            ObjectAnimator mAlphaAnim = ObjectAnimator.ofFloat(mImgMZ,"alpha", 0f, 1f);
            mAlphaAnim.setDuration(1500);
            mAlphaAnim.setRepeatCount(ObjectAnimator.INFINITE);
            mWeakAnims[1] = mAlphaAnim;
            ObjectAnimator bAlphaAnim = ObjectAnimator.ofFloat(mImgBZ,"alpha", 0f, 1f);
            bAlphaAnim.setDuration(1300);
            bAlphaAnim.setStartDelay(200);
            bAlphaAnim.setRepeatCount(ObjectAnimator.INFINITE);
            mWeakAnims[2] = bAlphaAnim;
        }
        mImgSZ.setVisibility(View.VISIBLE);
        mImgMZ.setVisibility(View.VISIBLE);
        mImgBZ.setVisibility(View.VISIBLE);

        int length = mWeakAnims.length;
        for (int i = 0; i < length; i++) {
            mWeakAnims[i].start();
        }

        mHandler.sendEmptyMessageDelayed(0, 3000);
    }

    private void startConnectAnim() {
        stopWeakAnim();
        mTxtPrompt.setText(FunSDK.TS("Device_Connectting"));
        if (mConnectAnims == null) {
            mConnectAnims = new Animator[2];
            ValueAnimator smallAlphaAnim = ValueAnimator.ofFloat(1.0f, 0.0f);
            mConnectAnims[0] = smallAlphaAnim;
            smallAlphaAnim.setDuration(1500);
            smallAlphaAnim.setRepeatCount(ValueAnimator.INFINITE);
            smallAlphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float alpha = (float) animation.getAnimatedValue();
                    mImgLeftS.setAlpha(alpha);
                    mImgRightS.setAlpha(alpha);
                }
            });
            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator bigShowAlphaAnim = ValueAnimator.ofFloat(0.4f, 1.0f);
            bigShowAlphaAnim.setDuration(200);
            ValueAnimator bigHideAlphaAnim = ValueAnimator.ofFloat(1.0f, 0.0f);
            bigHideAlphaAnim.setDuration(1300);
            bigHideAlphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float alpha = (float) animation.getAnimatedValue();
                    mImgRightB.setAlpha(alpha);
                    mImgLeftB.setAlpha(alpha);
                }
            });
            animatorSet.playSequentially(bigShowAlphaAnim, bigHideAlphaAnim);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mConnectAnims[1].start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mConnectAnims[1] = animatorSet;
        }
        mImgLeftS.setVisibility(View.VISIBLE);
        mImgLeftB.setAlpha(0.0f);
        mImgLeftB.setVisibility(View.VISIBLE);
        mImgRightS.setVisibility(View.VISIBLE);
        mImgRightB.setAlpha(0.0f);
        mImgRightB.setVisibility(View.VISIBLE);
        mConnectAnims[0].start();
        mConnectAnims[1].start();
    }

    private void stopConnectAnim() {
        if (mConnectAnims != null) {
            mConnectAnims[0].cancel();
            mConnectAnims[1].cancel();
        }
        mImgLeftS.setVisibility(View.INVISIBLE);
        mImgLeftB.setVisibility(View.INVISIBLE);
        mImgRightS.setVisibility(View.INVISIBLE);
        mImgRightB.setVisibility(View.INVISIBLE);
    }

    public void dismiss() {
        if (!isShow) {
            return;
        }
        isShow = false;
        mHandler.removeCallbacksAndMessages(null);
        stopWeakAnim();
        stopConnectAnim();
        if (mRootView == null || mRootView.get() == null) {
            return;
        }
        ((ViewGroup) mRootView.get()).removeView(mViewGroup);
    }

    public void destroy() {
        isShow = false;
        mHandler.removeCallbacksAndMessages(null);
        stopWeakAnim();
        stopConnectAnim();
        if (mRootView == null || mRootView.get() == null) {
            return;
        }
        ((ViewGroup) mRootView.get()).removeView(mViewGroup);
        this.mRootView.clear();
        mRootView = null;
    }
}


