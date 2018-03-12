package cn.com.mapuni.gis.meshingtotal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.tianditu.android.maps.GeoPoint;
import com.tianditu.android.maps.ItemizedOverlay;
import com.tianditu.android.maps.MapController;
import com.tianditu.android.maps.MapView;
import com.tianditu.android.maps.MapView.LayoutParams;
import com.tianditu.android.maps.MyLocationOverlay;
import com.tianditu.android.maps.Overlay;
import com.tianditu.android.maps.OverlayItem;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import cn.com.mapuni.gis.meshingtotal.model.JZYCBean;
import cn.com.mapuni.gis.meshingtotal.tdt.BaseMapActivityTDT;
import cn.com.mapuni.gis.meshingtotal.tdt.BaseMapActivityTDT.ViewCallback;
import cn.com.mapuni.meshing.base.controls.loading.YutuLoading;
import cn.com.mapuni.meshing.base.interfaces.PathManager;
import cn.com.mapuni.meshing.base.util.PollutionLevelCalUtil;

public class JzycActivity extends BaseMapActivityTDT implements
        ViewCallback, OnClickListener {
    private FrameLayout mView;
    private MapView mapView;
    private Context mContext;

    private TextView showHind;
    private LinearLayout news;
    private OverItemT mOverlay = null;
    public View mPopView = null;
    private ImageView ivClose;
    private TextView upload;
    private TextView biaoti1;
    private TextView biaoti2;
    private TextView biaoti3;
    private TextView biaoti4;
    private TextView biaoti5;
    private YutuLoading yutuLoading;
    List<JZYCBean> data;
    private JZYCBean bean;
    private ListView listView;
    private GeoPoint point;
    private int pageIndex = 1;
    private int Total;
    private Button leftButton, rightButton;
    private DbNewsAdapyer adapter;


    @Override
    public void onCreate(FrameLayout view, MapView mapView) {
        this.mapView = mapView;
        this.mView = view;
        this.mContext = this;
        data = new ArrayList<JZYCBean>();
        initData();

    }

    private void initData() {
        readRiskSources();
    }

    void initView() {
        mView.removeAllViews();
        // 查询条件
        View mainView = LayoutInflater.from(mContext).inflate(
                R.layout.jzyc_dbmainactivity_layout, null);
        mView.addView(mainView);
        leftButton = (Button) mainView.findViewById(R.id.left_button);
        rightButton = (Button) mainView.findViewById(R.id.rightbutton);
        biaoti1 = (TextView) mainView.findViewById(R.id.biaoti1);
        biaoti2 = (TextView) mainView.findViewById(R.id.biaoti2);
        biaoti3 = (TextView) mainView.findViewById(R.id.biaoti3);
        biaoti4 = (TextView) mainView.findViewById(R.id.biaoti4);
        biaoti5 = (TextView) mainView.findViewById(R.id.biaoti5);
        biaoti1.setText("序号");
        biaoti2.setText("监测点");
        biaoti3.setText("详情");
        biaoti4.setText("");
        biaoti4.setVisibility(View.GONE);
        biaoti5.setText("");
        biaoti5.setVisibility(View.GONE);

        // listview添加模拟数据
        listView = (ListView) mainView.findViewById(R.id.lv);


        adapter = new DbNewsAdapyer(mContext, data);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                mOverlay.setLastPoint(position);

            }
        });

        // ///////////////
        showHind = (TextView) mainView.findViewById(R.id.showhind);
        news = (LinearLayout) mainView.findViewById(R.id.news);
        showHind.setOnClickListener(this);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
    }

    void initMapView() {
        // 地图视图
        mapView.removeAllOverlay();// 清空所有覆盖物
        mapView.setBuiltInZoomControls(true);// 是否显示地图缩放按钮
        // 得到mMapView的控制权,可以用它控制和驱动平移和缩放
        MapController mMapController = mapView.getController();
        // 用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
        GeoPoint point = new GeoPoint((int) (116.915 * 1E6),
                (int) (117.404 * 1E6));
        // 设置地图中心点
        mMapController.setCenter(point);
        // 设置地图zoom级别
        mMapController.setZoom(12);
        // 添加我的位置和覆盖物集合

        List<Overlay> list = mapView.getOverlays();
        MyLocationOverlay myLocation = new MyLocationOverlay(mContext, mapView);
        myLocation.enableMyLocation();
        list.add(myLocation);
        Drawable markers[] = new Drawable[]{mContext.getResources().getDrawable(
                R.drawable.jiangong1), mContext.getResources().getDrawable(
                R.drawable.jiangong2), mContext.getResources().getDrawable(
                R.drawable.jiangong3), mContext.getResources().getDrawable(
                R.drawable.jiangong4), mContext.getResources().getDrawable(
                R.drawable.jiangong5), mContext.getResources().getDrawable(
                R.drawable.jiangong6), mContext.getResources().getDrawable(
                R.drawable.jiangong7)};
        Drawable marker = mContext.getResources().getDrawable(
                R.drawable.jiangong7);
        mPopView = LayoutInflater.from(mContext).inflate(
                R.layout.popview_jzyc, null);

        mOverlay = new OverItemT(marker, data, markers);


        list.add(mOverlay);
        // 创建弹出框view

        ivClose = (ImageView) mPopView.findViewById(R.id.close);
        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopView.setVisibility(View.GONE);
            }
        });
        mapView.addView(mPopView, new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, null,
                LayoutParams.TOP_LEFT));
        mPopView.setVisibility(View.GONE);
    }

    class OverItemT extends ItemizedOverlay<OverlayItem> implements
            Overlay.Snappable {
        /*GeoPoint point = new GeoPoint((int) (36.915 * 1E6),
                (int) (117.404 * 1E6));*/
        private List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();

        public OverItemT(Drawable marker, List<JZYCBean> zbdata, Drawable markers[]) {
            super(boundCenterBottom(marker));
            mapView.removeAllOverlay();
            for (int i = 0; i < zbdata.size(); i++) {
                OverlayItem item = new OverlayItem(
                        new GeoPoint((int) (zbdata.get(i).getLatitude() * 1E6),
                                (int) (zbdata.get(i).getLongitude() * 1E6)), "P" + i, "point"
                        + i);
                if(!TextUtils.isEmpty(zbdata.get(i).getPolluteRate())&&!"null".equals(zbdata.get(i).getPolluteRate())){
                    if (1== PollutionLevelCalUtil.getHourLevel("pm10",Double.parseDouble(zbdata.get(i).getPolluteRate()))) {
                        marker = markers[0];
                    } else if (2== PollutionLevelCalUtil.getHourLevel("pm10",Double.parseDouble(zbdata.get(i).getPolluteRate()))) {
                        marker = markers[1];
                    } else if (3== PollutionLevelCalUtil.getHourLevel("pm10",Double.parseDouble(zbdata.get(i).getPolluteRate()))) {
                        marker = markers[2];
                    } else if (4== PollutionLevelCalUtil.getHourLevel("pm10",Double.parseDouble(zbdata.get(i).getPolluteRate()))) {
                        marker = markers[3];
                    } else if (51== PollutionLevelCalUtil.getHourLevel("pm10",Double.parseDouble(zbdata.get(i).getPolluteRate()))) {
                        marker = markers[4];
                    } else if (6== PollutionLevelCalUtil.getHourLevel("pm10",Double.parseDouble(zbdata.get(i).getPolluteRate()))) {
                        marker = markers[5];
                    }
                }else {
                    marker = markers[6];
                }
                item.setMarker(marker);
                mGeoList.add(item);
            }
            populate();


        }

        public void setLastPoint(int i) {
            onTap(i);
        }

        @Override
        protected OverlayItem createItem(int i) {
            return mGeoList.get(i);
        }

        @Override
        public int size() {
            return mGeoList.size();
        }

        @Override
        protected boolean onTap(int i) {
            if (i == -1) {
                mPopView.setVisibility(View.GONE);
                return false;
            }
            if (data != null && data.size() > 0) {
                TextView tppe1 = (TextView) mPopView.findViewById(R.id.tv_type1);
                TextView tppe2 = (TextView) mPopView.findViewById(R.id.tv_type2);
                TextView title = (TextView) mPopView.findViewById(R.id.tv_title);
                TextView tppe3 = (TextView) mPopView.findViewById(R.id.tv_type3);
                TextView tppe4 = (TextView) mPopView.findViewById(R.id.tv_type4);
                tppe1.setText(data.get(i).getPolSorCode1());
                tppe2.setText(data.get(i).getAddress());
                tppe3.setText(data.get(i).getPolSorCode1());
                tppe4.setText(data.get(i).getPolluteRate());
                title.setText(data.get(i).getEntName());
            }

            GeoPoint pt = mGeoList.get(i).getPoint();
            mapView.updateViewLayout(mPopView, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, pt,
                    -3, -6, LayoutParams.BOTTOM_CENTER));
            mPopView.setVisibility(View.VISIBLE);
            mapView.getController().animateTo(pt);
            return true;
        }

        @Override
        public boolean onTap(GeoPoint p, MapView mapView1) {
            // mPopView.setVisibility(View.GONE);

            return super.onTap(p, mapView);
        }

    }

    @Override
    public void onClick(View arg0) {
        if (arg0.getId() == R.id.showhind) {
            if (news.getVisibility() == View.VISIBLE) {
                news.setVisibility(View.GONE);
            } else {
                news.setVisibility(View.VISIBLE);
            }
        } else if (arg0.getId() == R.id.left_button) {
            if (pageIndex <= 1) {
                Toast.makeText(this, "已经是第一页了", Toast.LENGTH_SHORT).show();
                return;
            }
            data.clear();
            pageIndex--;
            readRiskSources();

        } else if (arg0.getId() == R.id.rightbutton) {
            if(pageIndex>Total/20+1){
                Toast.makeText(this, "已经是最后一页了", Toast.LENGTH_SHORT).show();
                return;
            }
            data.clear();
            pageIndex++;
            readRiskSources();
        }
    }

    private void readRiskSources() {
        yutuLoading = new YutuLoading(this);
        yutuLoading.setCancelable(true);
        yutuLoading.setLoadMsg("正在获取信息，请稍候", "");
        yutuLoading.showDialog();
        //接口调用方法一
        String url = PathManager.JINAN_URL + PathManager.getAllContSpotsMonitorDataForAppInfo;
        RequestParams params = new RequestParams();// 添加提交参数
        params.addBodyParameter("PageIndex", pageIndex + "");
        params.addBodyParameter("PageSize", 20 + "");
        HttpUtils utils = new HttpUtils();
        utils.configCurrentHttpCacheExpiry(1000 * 60);
        utils.configTimeout(60 * 1000);//
        utils.configSoTimeout(60 * 1000);//
        utils.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                Toast.makeText(JzycActivity.this, "数据请求失败", Toast.LENGTH_SHORT).show();
                if (yutuLoading != null) {
                    yutuLoading.dismissDialog();
                }
//				updateList();
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                String result = String.valueOf(arg0.result);

                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(result.getBytes());
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(inputStream);
                    Element root = document.getRootElement();
                    JSONObject jsonObject = new JSONObject(root.getText());
                    JSONArray jsonArray = jsonObject.getJSONArray("rows");
                    JSONArray jsonArraytitle = jsonObject.getJSONArray("total");
                    if(jsonArraytitle!=null&&jsonArray.length() > 0){
                        Total=jsonArraytitle.getJSONObject(0).optInt("Column1",0);
                    }


                    if (jsonArray != null && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String EntName = jsonArray.getJSONObject(i).optString("EntName");
                            double Latitude = jsonArray.getJSONObject(i).optDouble("Latitude");
                            double Longitude = jsonArray.getJSONObject(i).optDouble("Longitude");
                            String Address = jsonArray.getJSONObject(i).optString("Address");
                            String PolSorCode1 = jsonArray.getJSONObject(i).optString("PM2P5");
                            String PolluteRate = jsonArray.getJSONObject(i).optString("PM10");
                            bean = new JZYCBean(EntName, Longitude, Latitude, Address, PolSorCode1, PolluteRate);
                            data.add(bean);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {

                }
                if (yutuLoading != null) {
                    yutuLoading.dismissDialog();
                }
                //updateList();
                initView();
                initMapView();
            }
        });
    }

    public class DbNewsAdapyer extends BaseAdapter {

        private Context context;
        private List<JZYCBean> list;

        public DbNewsAdapyer(Context context, List<JZYCBean> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return list.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(int i, View view, ViewGroup arg2) {
            ViewHolder viewHolder = null;
            if (view == null) {
                view = View.inflate(context, R.layout.jcqyc_list_item, null);
                ((LinearLayout) view).setGravity(Gravity.CENTER);
                viewHolder = new ViewHolder();
                viewHolder.grdrecodename = (TextView) view.findViewById(R.id.tv_id);
                viewHolder.gridpeople = (TextView) view
                        .findViewById(R.id.entname);
                viewHolder.details = (TextView) view
                        .findViewById(R.id.details);
                view.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            JZYCBean jzycBean = list.get(i);
            viewHolder.grdrecodename.setText(i + 1 + "");
            viewHolder.gridpeople.setText(jzycBean.getEntName());
            viewHolder.details.setText("详情");
            return view;
        }

        class ViewHolder {
            TextView grdrecodename;
            TextView gridpeople;
            TextView details;
        }
    }

}
