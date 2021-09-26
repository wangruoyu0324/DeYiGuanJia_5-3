package com.chuxinbuer.deyiguanjia.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chuxinbuer.deyiguanjia.R;
import com.chuxinbuer.deyiguanjia.mvp.model.KindModel;

import java.util.List;

/**
 * Created by wry on 2018-05-04 17:10
 */
public class KindAdapter extends BaseQuickAdapter<KindModel, BaseViewHolder> {

    public KindAdapter(List<KindModel> data) {
        super(R.layout.item_kind, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, KindModel bean) {
        helper.setImageResource(R.id.mImage, bean.getResId());
    }
}
