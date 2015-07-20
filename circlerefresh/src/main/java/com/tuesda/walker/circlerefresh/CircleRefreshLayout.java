package com.tuesda.walker.circlerefresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by zhanglei on 15/7/20.
 */
public class CircleRefreshLayout extends FrameLayout {

    private static String TAG = "pullToRefresh";

    private static final long BACK_TOP_DUR = 600;
    private static final long REL_DRAG_DUR = 200;

    private int mHeaderBackColor = 0xff8b90af;
    private int mHeaderForeColor = 0xffffffff;
    private int mHeaderCircleSmaller = 6;


    private float mPullHeight;
    private float mHeaderHeight;
    private View mChildView;
    private AnimationView mHeader;

    private boolean mIsRefreshing;

    private float mTouchStartY;

    private float mTouchCurY;

    private ValueAnimator mUpBackAnimator;
    private ValueAnimator mUpTopAnimator;

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(10);

    public CircleRefreshLayout(Context context) {
        this(context, null, 0);
    }

    public CircleRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        if (getChildCount() > 1) {
            throw new RuntimeException("you can only attach one child");
        }
        setAttrs(attrs);
        mPullHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
        mHeaderHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());

        this.post(new Runnable() {
            @Override
            public void run() {
                mChildView = getChildAt(0);
                addHeaderView();
            }
        });

    }

    private void setAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CirCleRefreshLayout);

        mHeaderBackColor = a.getColor(R.styleable.CirCleRefreshLayout_AniBackColor, mHeaderBackColor);
        mHeaderForeColor = a.getColor(R.styleable.CirCleRefreshLayout_AniForeColor, mHeaderForeColor);
        mHeaderCircleSmaller = a.getInt(R.styleable.CirCleRefreshLayout_CircleSmaller, mHeaderCircleSmaller);

        a.recycle();
    }

    private void addHeaderView() {
        mHeader = new AnimationView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.gravity = Gravity.TOP;
        mHeader.setLayoutParams(params);

        addViewInternal(mHeader);
        mHeader.setAniBackColor(mHeaderBackColor);
        mHeader.setAniForeColor(mHeaderForeColor);
        mHeader.setRadius(mHeaderCircleSmaller);

        setUpChildAnimation();
    }

    private void setUpChildAnimation() {
        if (mChildView == null) {
            return;
        }
        mUpBackAnimator = ValueAnimator.ofFloat(mPullHeight, mHeaderHeight);
        mUpBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                if (mChildView != null) {
                    mChildView.setTranslationY(val);
                }
            }
        });
        mUpBackAnimator.setDuration(REL_DRAG_DUR);
        mUpTopAnimator = ValueAnimator.ofFloat(mHeaderHeight, 0);
        mUpTopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                val = decelerateInterpolator.getInterpolation(val / mHeaderHeight) * val;
                if (mChildView != null) {
                    mChildView.setTranslationY(val);
                }
                mHeader.getLayoutParams().height = (int) val;
                mHeader.requestLayout();
            }
        });
        mUpTopAnimator.setDuration(BACK_TOP_DUR);

        mHeader.setOnViewAniDone(new AnimationView.OnViewAniDone() {
            @Override
            public void viewAniDone() {
//                Log.i(TAG, "should invoke");
                mUpTopAnimator.start();
            }
        });


    }

    private void addViewInternal(@NonNull View child) {
        super.addView(child);
    }

    @Override
    public void addView(View child) {
        if (getChildCount() >= 1) {
            throw new RuntimeException("you can only attach one child");
        }

        mChildView = child;
        super.addView(child);
        setUpChildAnimation();
    }

    private boolean canChildScrollUp() {
        if (mChildView == null) {
            return false;
        }


        return ViewCompat.canScrollVertically(mChildView, -1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsRefreshing) {
            return true;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartY = ev.getY();
                mTouchCurY = mTouchStartY;
                break;
            case MotionEvent.ACTION_MOVE:
                float curY = ev.getY();
                float dy = curY - mTouchStartY;
                if (dy > 0 && !canChildScrollUp()) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsRefreshing) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mTouchCurY = event.getY();
                float dy = mTouchCurY - mTouchStartY;
                dy = Math.min(mPullHeight * 2, dy);
                dy = Math.max(0, dy);


                if (mChildView != null) {
                    float offsetY = decelerateInterpolator.getInterpolation(dy / 2 / mPullHeight) * dy / 2;
                    mChildView.setTranslationY(offsetY);

                    mHeader.getLayoutParams().height = (int) offsetY;
                    mHeader.requestLayout();
                }


                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mChildView != null) {
                    if (mChildView.getTranslationY() >= mHeaderHeight) {
                        mUpBackAnimator.start();
                        mHeader.releaseDrag();
                        mIsRefreshing = true;
                        if (onCircleRefreshListener!=null) {
                            onCircleRefreshListener.refreshing();
                        }

                    } else {
                        float height = mChildView.getTranslationY();
                        ValueAnimator backTopAni = ValueAnimator.ofFloat(height, 0);
                        backTopAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float val = (float) animation.getAnimatedValue();
                                val = decelerateInterpolator.getInterpolation(val / mHeaderHeight) * val;
                                if (mChildView != null) {
                                    mChildView.setTranslationY(val);
                                }
                                mHeader.getLayoutParams().height = (int) val;
                                mHeader.requestLayout();
                            }
                        });
                        backTopAni.setDuration((long) (height * BACK_TOP_DUR / mHeaderHeight));
                        backTopAni.start();
                    }
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public void finishRefreshing() {
        if (onCircleRefreshListener != null) {
            onCircleRefreshListener.completeRefresh();
        }
        mIsRefreshing = false;
        mHeader.setRefreshing(false);
    }

    private OnCircleRefreshListener onCircleRefreshListener;

    public void setOnRefreshListener(OnCircleRefreshListener onCircleRefreshListener) {
        this.onCircleRefreshListener = onCircleRefreshListener;
    }

    public interface OnCircleRefreshListener {
        void completeRefresh();

        void refreshing();
    }
}
