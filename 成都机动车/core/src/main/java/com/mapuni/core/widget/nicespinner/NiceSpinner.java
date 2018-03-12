package com.mapuni.core.widget.nicespinner;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mapuni.core.R;

import java.util.Arrays;
import java.util.List;

/**
 * @author angelo.marchesin
 */
@SuppressWarnings("unused")
public class NiceSpinner extends TextView {

    private static final int MAX_LEVEL = 10000;
    private static final int DEFAULT_ELEVATION = 16;
    private static final String INSTANCE_STATE = "instance_state";
    private static final String SELECTED_INDEX = "selected_index";
    private static final String IS_POPUP_SHOWING = "is_popup_showing";

    private int selectedIndex;
    private Drawable drawable;
    private PopupWindow popupWindow;
    private ListView listView;
    private NiceSpinnerAdapter adapter;
    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private boolean isArrowHide;
    private int textColor;
    private int backgroundSelector;
    private int listBackground;
    private int divider;
    private int textSize;
    private IconChange iconChange;

    @SuppressWarnings("ConstantConditions")
    public NiceSpinner(Context context) {
        super(context);
        init(context, null);
    }

    public NiceSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NiceSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    public void setIconChange(IconChange ic){
        iconChange = ic;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(SELECTED_INDEX, selectedIndex);
        if (popupWindow != null) {
            bundle.putBoolean(IS_POPUP_SHOWING, popupWindow.isShowing());
            dismissDropDown();
        }
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable savedState) {
        if (savedState instanceof Bundle) {
            Bundle bundle = (Bundle) savedState;
            selectedIndex = bundle.getInt(SELECTED_INDEX);

            if (adapter != null) {
                setText(adapter.getItemInDataset(selectedIndex).toString());
                adapter.notifyItemSelected(selectedIndex);
            }

            if (bundle.getBoolean(IS_POPUP_SHOWING)) {
                if (popupWindow != null) {
                    // Post the show request into the looper to avoid bad token exception
                    post(new Runnable() {
                        @Override
                        public void run() {
                            showDropDown();
                        }
                    });
                }
            }
            savedState = bundle.getParcelable(INSTANCE_STATE);
        }
        super.onRestoreInstanceState(savedState);
    }


    private void init(Context context, AttributeSet attrs) {
        Resources resources = getResources();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NiceSpinner);
        int defaultPadding = resources.getDimensionPixelSize(R.dimen.one_and_a_half_grid_unit);
        setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        setPadding(resources.getDimensionPixelSize(R.dimen.three_grid_unit), 5, 5, 5);
        setClickable(true);
        backgroundSelector = typedArray.getResourceId(R.styleable.NiceSpinner_backgroundSelector, 0);
//        listBackground = typedArray.getResourceId(R.styleable.NiceSpinner_listbackground, R.drawable.rect);
        listBackground = typedArray.getResourceId(R.styleable.NiceSpinner_listbackground, R.color.check);
        divider = typedArray.getResourceId(R.styleable.NiceSpinner_dividerLine, R.drawable.divider);
        if(backgroundSelector!=0)
            setBackgroundResource(backgroundSelector);
        textColor = typedArray.getColor(R.styleable.NiceSpinner_textTint,getResources().getColor(R.color.white));
        setTextColor(textColor);
        listView = new ListView(context);
        // Set the spinner's id into the listview to make it pretend to be the right parent in
        // onItemClick
        listView.setId(getId());
        listView.setDivider(getResources().getDrawable(divider));
        listView.setItemsCanFocus(true);
        //hide vertical and horizontal scrollbars
        listView.setBackgroundResource(listBackground);
        listView.setVerticalScrollBarEnabled(false);
        listView.setHorizontalScrollBarEnabled(false);
        listView.setPadding(0,0,0,0);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= selectedIndex && position < adapter.getCount()) {
                    position++;
                }

                // Need to set selected index before calling listeners or getSelectedIndex() can be
                // reported incorrectly due to race conditions.
                selectedIndex = position;

                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(parent, view, position, id);
                }

                if (onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(parent, view, position, id);
                }

                adapter.notifyItemSelected(position);
                setText(adapter.getItemInDataset(position).toString());
                dismissDropDown();
            }
        });
        popupWindow = new PopupWindow(context);

        popupWindow.setContentView(listView);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            popupWindow.setElevation(DEFAULT_ELEVATION);
//            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.spinner_drawable));
//        } else {
//            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.drop_down_shadow));
//        }

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!isArrowHide) {
                    animateArrow(false);
                }
            }
        });

        isArrowHide = typedArray.getBoolean(R.styleable.NiceSpinner_hideArrow, false);
        if (!isArrowHide) {
            Drawable basicDrawable = ContextCompat.getDrawable(context, R.drawable.arrow);
            int resId = typedArray.getColor(R.styleable.NiceSpinner_arrowTint, -1);
            if (basicDrawable != null) {
                drawable = DrawableCompat.wrap(basicDrawable);
                if (resId != -1) {
                    DrawableCompat.setTint(drawable, resId);
                }
            }
         //   setCompoundDrawablesWithIntrinsicBounds(null, null,drawable, null);
        }
        final CharSequence[] entries = typedArray.getTextArray(R.styleable.NiceSpinner_entries);
        if (entries != null) {
            attachDataSource(Arrays.asList(entries));
        }

        typedArray.recycle();
    }

    public void setBackGroundResource(int bgSource){
        backgroundSelector = bgSource;
        setBackgroundResource(backgroundSelector);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Set the default spinner item using its index
     * 
     * @param position the item's position
     */
    public void setSelectedIndex(int position) {
        if (adapter != null) {
            if (position >= 0 && position <= adapter.getCount()) {
                adapter.notifyItemSelected(position);
                selectedIndex = position;
                setText(adapter.getItemInDataset(position).toString());
            } else {
                throw new IllegalArgumentException("Position must be lower than adapter count!");
            }
        }
    }

    public void addOnItemClickListener(@NonNull AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectedListener(@NonNull AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public <T> void attachDataSource(@NonNull List<T> dataset) {
        int bgSelector = R.drawable.kuang;
        adapter = new NiceSpinnerAdapter<>(getContext(), dataset, textColor, bgSelector);
        setAdapterInternal(adapter);
    }

//    public void setAdapter(@NonNull ListAdapter adapter) {
//        this.adapter = new NiceSpinnerAdapterWrapper(getContext(), adapter, textColor, backgroundSelector);
//        setAdapterInternal(this.adapter);
//    }

    private void setAdapterInternal(@NonNull NiceSpinnerAdapter adapter) {
        // If the adapter needs to be settled again, ensure to reset the selected index as well
        selectedIndex = 0;
        listView.setAdapter(adapter);
        setText(adapter.getItemInDataset(selectedIndex).toString());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        popupWindow.setWidth(MeasureSpec.getSize(widthMeasureSpec));
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!popupWindow.isShowing()) {
                showDropDown();
            } else {
                dismissDropDown();
            }
        }
        return super.onTouchEvent(event);
    }

    private void animateArrow(boolean shouldRotateUp) {
        if(shouldRotateUp){
            if(iconChange!=null)
                iconChange.spinnerPress();
        }else{
            if(iconChange!=null)
                iconChange.spinner();
        }
        int start = shouldRotateUp ? 0 : MAX_LEVEL;
        int end = shouldRotateUp ? MAX_LEVEL : 0;
        ObjectAnimator animator = ObjectAnimator.ofInt(drawable, "level", start, end);
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.start();
    }

    public void dismissDropDown() {
        if (!isArrowHide) {
            animateArrow(false);
        }
        popupWindow.dismiss();
    }

    public void showDropDown() {
        if (!isArrowHide) {
            animateArrow(true);
        }
        popupWindow.showAsDropDown(this);
    }

    public void setTintColor(@ColorRes int resId) {
        if (drawable != null && !isArrowHide) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), resId));
        }
    }
    public static interface IconChange{
        public void spinner();
        public void spinnerPress();
    }
}
