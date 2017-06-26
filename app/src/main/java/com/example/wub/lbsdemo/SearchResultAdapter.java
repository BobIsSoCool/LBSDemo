package com.example.wub.lbsdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.utils.DistanceUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wub on 2017/6/23.
 */
public class SearchResultAdapter extends BaseAdapter {

    /**
     * 上下文
     */
    Context context;
    /**
     * 数据
     */
    private List<SuggestionResult.SuggestionInfo> mList = new ArrayList<>();
    /**
     * 当前位置
     */
    private LatLng currentLatl;

    /**
     * @param context
     * @param list
     */
    public SearchResultAdapter(Context context, List<SuggestionResult.SuggestionInfo> list) {
        this.context = context;
        this.mList = list;
    }

    public void setCurrentLatl(LatLng latl) {
        currentLatl = latl;
    }

    /**
     * @return
     */
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.item_search_result, null);
            viewHolder.tvInfo = (TextView) view.findViewById(R.id.tvInfo);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        LatLng anotherLatl = mList.get(i).pt;
        //计算该地点与当前位置间直线距离
        double distance = DistanceUtil.getDistance(currentLatl, anotherLatl);

        StringBuilder sb = new StringBuilder();
        sb.append(mList.get(i).key);


        if (distance >= 1000) {
            double d1 = distance / 1000;
            //四舍五入保留两位小数
            BigDecimal bg = new BigDecimal(d1);
            double d = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            sb.append(d).append("km");
        } else {
            BigDecimal bg = new BigDecimal(distance);
            double d = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            sb.append(d).append("m");
        }


        //显示信息
        viewHolder.tvInfo.setText(sb);


        return view;
    }

    class ViewHolder {
        /**
         * 位置信息描述
         */
        TextView tvInfo;
    }
}
