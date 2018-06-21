package cn.lmcw.imagereview.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.lmcw.imagereview.R;

/**
 * Created by Administrator on 2018/6/20.
 */

public class ImagePageAdapter extends PagerAdapter {

    private List<Object> images = new ArrayList<>();

    private Activity mActivity;
    public HashMap<Integer, View> itemViews = new HashMap<Integer, View>();

    private PhotoViewListener listener;
    private PhotoViewLoaderListener loaderListener;


    public ImagePageAdapter(Activity activity, ArrayList<Object> images) {
        this.mActivity = activity;
        this.images = images;
    }

    public void setData(ArrayList<Object> images) {
        this.images = images;
    }

    public void setPhotoViewListener(PhotoViewListener listener) {
        this.listener = listener;
    }

    public PhotoViewListener getPhotoViewClickListener() {
        return this.listener;
    }

    public void setLoaderListener(PhotoViewLoaderListener loaderListener) {
        this.loaderListener = loaderListener;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        View view = View.inflate(mActivity, R.layout.item_review_layout, null);

        PhotoView photoView = (PhotoView) view.findViewById(R.id.img_view);

        Object imageItem = images.get(position);
        if (imageItem != null)
            //加载图片
            loaderListener.displayImage(photoView, imageItem);
        // 添加点击事件
        photoView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (listener != null) listener.OnPhotoClickListener(v);
            }
        });

        //长按事件
        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) listener.OnPhotoLongClick(v, position);
                return false;
            }
        });

        itemViews.put(position, view);
        container.addView(view);

        return view;
    }

    public PhotoView getImageView(int position) {

        return (PhotoView) itemViews.get(position).findViewById(R.id.img_view);
    }



    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        itemViews.remove(position);
        container.removeView((View) object);
    }


    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    public interface PhotoViewListener {

        void OnPhotoClickListener(View view);

        void OnPhotoLongClick(View view, int position);

        void OnPhotoClose(int position);

    }

    public interface PhotoViewLoaderListener {

        void displayImage(ImageView imageView, Object object);

    }
}

