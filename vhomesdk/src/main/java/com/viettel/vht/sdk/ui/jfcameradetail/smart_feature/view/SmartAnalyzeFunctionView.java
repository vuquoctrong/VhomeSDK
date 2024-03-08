package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.viettel.vht.sdk.R;
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.model.FunctionViewItemElement;

import java.util.List;


/**
 * Created by zhangyongyong on 2017-05-08-10:53.
 */

public class SmartAnalyzeFunctionView extends RelativeLayout implements FunctionViewAdapter.OnItemClickListener {

    private final String TAG = SmartAnalyzeFunctionView.class.getSimpleName();

    private RecyclerView recyclerView;
    private AdaptiveLayoutManager mLayoutManager;
    private Context mContext;
    private FunctionViewAdapter mViewAdapter;
    private OnItemClickListener mItemClickListener;
    private int directionPos = -1;
    private List<FunctionViewItemElement> functionList;
    public SmartAnalyzeFunctionView(Context context, List<FunctionViewItemElement> functionList) {
        super(context);
        this.mContext = context;
        this.functionList = functionList;
    }
    public SmartAnalyzeFunctionView(Context context , AttributeSet attrs){
        super(context, attrs);
        this.mContext = context;
    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView(List<FunctionViewItemElement> functionList) {
        recyclerView = new RecyclerView(mContext);
        recyclerView.setBackground(mContext.getResources().getDrawable(R.drawable.smart_analyze_item_bg));
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(CENTER_IN_PARENT);
        recyclerView.setLayoutParams(layoutParams);
        mLayoutManager = new AdaptiveLayoutManager(mContext);
        mLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(mLayoutManager);
        mViewAdapter = new FunctionViewAdapter();
        mViewAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mViewAdapter);
        addView(recyclerView);

        initData();
    }

    private void initData() {
        if (directionPos == -1) {
            directionPos = 0;
        }

        mViewAdapter.setItemSelected(directionPos);
    }

    public void setData(List<FunctionViewItemElement> List) {
        Log.d(TAG, "setData: " + new Gson().toJson(List));
        this.functionList = List;
        initView(functionList);
        mViewAdapter.submitList(functionList);
    }

    @Override
    public void onItemClick(View view, int position, String label) {
        mViewAdapter.setItemSelected(position);
        if (mItemClickListener != null) {
            mItemClickListener.onItemClick(view, position, label);
        }
    }

    public void setItemSelected(int position) {
        this.directionPos = position;
        if (mViewAdapter != null) {
           mViewAdapter.setItemSelected(position);
        }
    }

    public void setItemUnSelected() {
        this.directionPos = -1;
        if (mViewAdapter != null) {
            mViewAdapter.setItemUnSelected();
        }
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String label);
    }
}
