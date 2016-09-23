package com.dopstore.mall.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dopstore.mall.R;
import com.dopstore.mall.activity.bean.MainMiddleData;
import com.dopstore.mall.activity.bean.ShopData;
import com.dopstore.mall.shop.activity.ShopDetailActivity;
import com.dopstore.mall.util.Constant;
import com.dopstore.mall.util.LoadImageUtils;
import com.dopstore.mall.util.SkipUtils;
import com.dopstore.mall.util.Utils;
import com.dopstore.mall.view.EScrollView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 喜成 on 16/9/5.
 * name
 */
public class MiddleAdapter extends BaseAdapter {
    private Context context;
    private List<MainMiddleData> list;
    private List<ShopData> datas;
    private LoadImageUtils loadImageUtils;

    public MiddleAdapter(Context context, List<MainMiddleData> list) {
        this.context = context;
        this.list = list;
        loadImageUtils = LoadImageUtils.getInstance(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_main_middle_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.bigImageView = (ImageView) view.findViewById(R.id.main_middle_bigimage);
            viewHolder.eScrollView = (EScrollView) view.findViewById(R.id.main_middle_escrollview);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        loadImageUtils.displayImage(list.get(i).getPicture(), viewHolder.bigImageView);

        datas=list.get(i).getRelated_goods();
        viewHolder.eScrollView.setAdapter(new MiddleDataAdapter(context, datas));
        viewHolder.eScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ShopData data=datas.get(i);
                Map<String,Object> map=new HashMap<String, Object>();
                map.put(Constant.LIST,data);
                SkipUtils.jumpForMap(context, ShopDetailActivity.class,map,false);
            }
        });

        return view;
    }

    public void upData(List<MainMiddleData> midddleList) {
        this.list=midddleList;
        notifyDataSetChanged();
    }

    class ViewHolder {
        private ImageView bigImageView;
        private EScrollView eScrollView;
    }
}
