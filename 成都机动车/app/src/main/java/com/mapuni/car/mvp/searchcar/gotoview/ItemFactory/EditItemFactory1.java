package com.mapuni.car.mvp.searchcar.gotoview.ItemFactory;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.mapuni.car.R;
import com.mapuni.car.mvp.detailcar.model.EditValueBean1;
import com.mapuni.car.mvp.searchcar.gotoview.presenter.CarListPresenter;
import com.mapuni.car.mvp.searchcar.gotoview.presenter.CarListReciverPresenter;
import com.mapuni.core.assemblyadapter.AssemblyRecyclerItem;
import com.mapuni.core.assemblyadapter.AssemblyRecyclerItemFactory;

/**
 * Created by yawei on 2017/8/25.
 */

public class EditItemFactory1 extends AssemblyRecyclerItemFactory<EditItemFactory1.CarItem> {

    @Override
    public boolean isTarget(Object data) {
        return data instanceof EditValueBean1;
    }


    @Override
    public CarItem createAssemblyItem(ViewGroup parent) {
        return new CarItem(R.layout.item_edit1_layout,parent);
    }

    public class CarItem extends AssemblyRecyclerItem<EditValueBean1> {
        TextView name, timeSelect, divider,Tvflag;
        EditText value;
        Context context;

        public CarItem(int itemLayoutId, ViewGroup parent) {
            super(itemLayoutId, parent);
        }

        @Override
        protected void onFindViews() {
            name = (TextView) findViewById(R.id.name);
            value = (EditText) findViewById(R.id.value);
            Tvflag = (TextView) findViewById(R.id.flag);
            divider = (TextView) findViewById(R.id.divider);
        }

        @Override
        protected void onConfigViews(Context context) {
            this.context = context;
            value.addTextChangedListener(new MyEditTextChangeListener());
        }

        @Override
        protected void onSetData(int position, EditValueBean1 bean) {
                if (bean.getColor() != null) {
                    value.setTextColor(Color.parseColor(bean.getColor()));
//                value.setTextColor(context.getResources().getColor(R.color.Blue));
                }
               // value.setFilters(new InputFilter[]{new InputFilter.LengthFilter((Integer.parseInt(bean.getMaxLength())))});
                name.setText(bean.getName());
                value.setText(bean.getValue());
            if("true".equals(bean.getFlag())){
                Tvflag.setVisibility(View.VISIBLE);
            }else {
                Tvflag.setVisibility(View.GONE);
            }
            CarListPresenter.setParams(getData().getKey(),value.getText().toString());
            CarListReciverPresenter.setParams(getData().getKey(),value.getText().toString());
        }

        public class MyEditTextChangeListener implements TextWatcher {
            /**
             * 编辑框的内容发生改变之前的回调方法
             */
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            /**
             * 编辑框的内容正在发生改变时的回调方法 >>用户正在输入
             * 我们可以在这里实时地 通过搜索匹配用户的输入
             */
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            /**
             * 编辑框的内容改变以后,用户没有继续输入时 的回调方法
             */
            @Override
            public void afterTextChanged(Editable editable) {
                getData().setValue(value.getText().toString());
                CarListPresenter.setParams(getData().getKey(),value.getText().toString());
                CarListReciverPresenter.setParams(getData().getKey(),value.getText().toString());
            }
        }
    }


}
