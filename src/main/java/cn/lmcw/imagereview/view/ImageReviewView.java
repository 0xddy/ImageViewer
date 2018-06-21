package cn.lmcw.imagereview.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;

import com.github.chrisbanes.photoview.PhotoView;

import cn.lmcw.imagereview.adapter.ImagePageAdapter;

/**
 * Created by Administrator on 2018/6/20.
 */

public class ImageReviewView extends ViewPager {


    private ImagePageAdapter imagePageAdapter;

    private int displayHeight;

    private boolean animRunning = false;

    //竖向移动中
    private boolean verticalMove = false;
    //viewpager 滑动
    private boolean viewpagerMove = false;

    private SimpleOnPageChangeListener pageChangeListener;

    float downY = 0;
    float downX = 0;
    //photoView初始的位置
    float photoViewDefaultY;
    //当前背景颜色
    int currBgColor;

    //当前选中的页面
    int _position = 0;

    //动画退出时间
    private final int outAnimTime = 500;

    public ImageReviewView(Context context) {
        super(context);
    }

    public ImageReviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0xff000000);
        displayHeight = getDisplayHeight();

        if (pageChangeListener != null)
            removeOnPageChangeListener(pageChangeListener);
        addOnPageChangeListener(pageChangeListener = new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                _position = position;

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == SCROLL_STATE_IDLE) {
                    viewpagerMove = false;
                }
            }
        });

    }

    public void attach(ImagePageAdapter imagePageAdapter) {
        this.imagePageAdapter = imagePageAdapter;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downY = ev.getY();
            downX = ev.getX();
            //一开始默认位置
            photoViewDefaultY = getImageView(_position).getY();

        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {

            if (getImageView(_position).getScale() == 1.0) {
                return true;
            }

        } else if (ev.getAction() == MotionEvent.ACTION_UP) {

            if (getImageView(_position).getScale() == 1.0) {
                return true;
            }

        }

        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (animRunning) {
            //动画运行中，不处理事件，防止View出现错乱
            return false;
        }

        if (ev.getAction() == MotionEvent.ACTION_MOVE) {

            //左右滑动的时候不执行
            if (Math.abs(downX - ev.getX()) > 50 && Math.abs(downY - ev.getY()) < 200 && !verticalMove) {
                viewpagerMove = true;
                return super.onTouchEvent(ev);
            }

            if (viewpagerMove) {
                return super.onTouchEvent(ev);
            }

            verticalMove = true;

            float diff;
            float photoViewY;
            if (downY > ev.getY()) {
                diff = downY - ev.getY();
                photoViewY = getImageView(_position).getY() - diff;
            } else {
                diff = ev.getY() - downY;
                photoViewY = getImageView(_position).getY() + diff;
            }
            Log.i("MyView", downY + " " + photoViewY);
            //设置位置
            getImageView(_position).setY(photoViewY);

            //相对于初始位置移动的距离
            float dist = Math.abs(photoViewDefaultY - photoViewY);

            //屏幕高度0.7作为比例
            float b = dist / displayHeight * 0.7f;
            if (b > 1f)
                b = 1f;

            int fraction = (int) (255 * (1 - b));

            if (fraction > 255)
                fraction = 255;
            if (fraction < 0)
                fraction = 0;
            //改变背景色
            setBackgroundColor(currBgColor = Color.argb(fraction, 0, 0, 0));
            downY = ev.getY();

            return true;

        } else if (ev.getAction() == MotionEvent.ACTION_UP) {

            verticalMove = false;
            //重置move状态需要在viewpager滑动动画完成后
            //viewpagerMove = false;

            float diff = photoViewDefaultY - getImageView(_position).getY();

            float dist = Math.abs(diff);

            if (dist > displayHeight * 0.3) {
                //下拉距离超过一半，就直接消失掉

                float starY = getImageView(_position).getY();
                float endY = 0;

                if (diff > 0) {
                    //向上
                    endY = -getImageView(_position).getHeight();
                } else {
                    //向下
                    endY = displayHeight;
                }

                ObjectAnimator translationAnim = ObjectAnimator.ofFloat(imagePageAdapter.getImageView(_position), "Y", starY, endY);
                int starColor = currBgColor;
                int endColor = 0x00000000;

                ObjectAnimator colorAnim = ObjectAnimator.ofInt(this, "backgroundColor", starColor, endColor);
                colorAnim.setEvaluator(new ArgbEvaluator());

                AnimatorSet set = new AnimatorSet();
                long t = (long) (outAnimTime * (dist / (displayHeight / 2)));
                Log.i("MyView", t + "");
                set.setDuration(t);
                set.play(translationAnim).with(colorAnim);
                set.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        animRunning = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animRunning = false;
                        if (imagePageAdapter.getPhotoViewClickListener() != null)
                            //回调关闭，返回当前操作的位置
                            imagePageAdapter.getPhotoViewClickListener().OnPhotoClose(_position);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                set.start();

            } else {
                getImageView(_position).setY(photoViewDefaultY);
                setBackgroundColor(Color.argb(255, 0, 0, 0));
            }

        }

        return super.onTouchEvent(ev);
    }

    public PhotoView getImageView(int key) {
        return imagePageAdapter.getImageView(key);
    }

    private int getDisplayHeight() {

        DisplayMetrics dm = getResources().getDisplayMetrics();
        return dm.heightPixels;
    }


    private class ArgbEvaluator implements TypeEvaluator {
        //这段代码是从源码中抠出来的
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            int startInt = (Integer) startValue;
            int startA = (startInt >> 24) & 0xff;
            int startR = (startInt >> 16) & 0xff;
            int startG = (startInt >> 8) & 0xff;
            int startB = startInt & 0xff;

            int endInt = (Integer) endValue;
            int endA = (endInt >> 24) & 0xff;
            int endR = (endInt >> 16) & 0xff;
            int endG = (endInt >> 8) & 0xff;
            int endB = endInt & 0xff;

            return (int) ((startA + (int) (fraction * (endA - startA))) << 24) |
                    (int) ((startR + (int) (fraction * (endR - startR))) << 16) |
                    (int) ((startG + (int) (fraction * (endG - startG))) << 8) |
                    (int) ((startB + (int) (fraction * (endB - startB))));
        }
    }

}

