package cn.lmcw.imagereview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.Date;

import cn.lmcw.imagereview.adapter.ImagePageAdapter;
import cn.lmcw.imagereview.utils.FileUtils;
import cn.lmcw.imagereview.view.ImageReviewView;

public class ImageViewerActivity extends AppCompatActivity {

    ImageReviewView imageReviewView;
    //页码
    TextView txtNum;


    int POSITION = 0;
    ArrayList<Object> IMAGES = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏操作
        fullScreen();
        setContentView(R.layout.activity_image_viewer);

        parseIntent();
        initView();

    }

    private void parseIntent() {
        IMAGES = (ArrayList<Object>) getIntent().getSerializableExtra("IMAGES");
        POSITION = getIntent().getIntExtra("POSITION", 0);

        if (IMAGES == null)
            Toast.makeText(this, "暂无图片", Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        imageReviewView = (ImageReviewView) findViewById(R.id.image_review_view);
        txtNum = (TextView) findViewById(R.id.txt_page);

        txtNum.setText("1/" + IMAGES.size());

        ImagePageAdapter imagePageAdapter = new ImagePageAdapter(this, IMAGES);

        imageReviewView.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                txtNum.setText((position + 1) + "/" + IMAGES.size());
            }
        });


        imagePageAdapter.setLoaderListener(new ImagePageAdapter.PhotoViewLoaderListener() {
            @Override
            public void displayImage(ImageView photoView, Object object) {
                Glide.with(ImageViewerActivity.this).load(object).into(photoView);
            }
        });

        imagePageAdapter.setPhotoViewListener(new ImagePageAdapter.PhotoViewListener() {

            public void OnPhotoClickListener(View view) {
                //Toast.makeText(ImageViewerActivity.this, "PhotoTap", Toast.LENGTH_SHORT).show();
            }

            public void OnPhotoLongClick(View view, final int position) {
                //Toast.makeText(ImageViewerActivity.this, "LongClick", Toast.LENGTH_SHORT).show();

                //长按图片
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                dialogBuilder.setMessage("保存当前图片至手机吗？");
                dialogBuilder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Glide.with(getContext()).load(IMAGES.get(position)).asBitmap().toBytes().into(new SimpleTarget<byte[]>() {
                            @Override
                            public void onResourceReady(byte[] bytes, GlideAnimation<? super byte[]> glideAnimation) {
                                try {
                                    String filePath = FileUtils.savaBitmap(getContext(), new Date().getTime() + ".jpg", bytes);

                                    if (filePath == null)
                                        Toast.makeText(getContext(), "请检查SD卡是否可用", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getContext(), "图片已保存到 " + filePath, Toast.LENGTH_SHORT).show();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });

                dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialogBuilder.show();

            }

            public void OnPhotoClose(int position) {
                //Toast.makeText(ImageViewerActivity.this, "close", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        imageReviewView.attach(imagePageAdapter);
        imageReviewView.setAdapter(imagePageAdapter);
        if (POSITION > 0 && POSITION < IMAGES.size())
            imageReviewView.setCurrentItem(POSITION);
    }


    private void fullScreen() {
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private Context getContext() {
        return this;
    }

    public static void start(Intent extras, Context context) {
        Intent intent = new Intent();
        intent.setClass(context, ImageViewerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    public static void start(Context context, ArrayList<Object> images, int position) {
        Intent intent = new Intent();
        intent.putExtra("IMAGES", images);
        intent.putExtra("POSITION", position);
        start(intent, context);
    }
}
