package com.qinglu.ad;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.guang.client.GCommon;
import com.guang.client.tools.GTools;
import com.qinglu.ad.view.GEventWindowView;
import com.sdk.callback.DataCallback;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pa.path.Entrance;
import pa.path.entity.SDKData;

/**
 * Created by guang on 2017/9/18.
 */

public class QLNewsHand {
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;
    private static Service context;
    private static QLNewsHand _instance;
    private boolean isShow = false;

    private RelativeLayout rel;
    private GEventWindowView root;

    private int initY = 0;
    private int initX = 0;
    private int initRX = 0;
    private int harfX = 0;
    private Handler handler;
    private Handler handlerPop;
    private long dt = 40;

    private String channel = "0fb6d980-9d08-11e7-9468-00163e001936";

    private List<Country> countryList = null;
    private Country currCountry;
    private List<NewsItem> newsItemList = new ArrayList<NewsItem>();
    private List<SDKData> adList = new ArrayList<SDKData>();
    private boolean isDrag = false;

    private QLNewsHand(){}

    public static QLNewsHand getInstance()
    {
        if(_instance == null)
        {
            _instance = new QLNewsHand();
        }
        return _instance;
    }

    @SuppressLint({ "NewApi", "ResourceAsColor" })
    public void show() {
        this.context = (Service) QLAdController.getInstance().getContext();
        isShow = true;
        wmParams = new WindowManager.LayoutParams();
        // 获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) context.getApplication()
                .getSystemService(context.getApplication().WINDOW_SERVICE);
        // 设置window type
        if (android.os.Build.VERSION.SDK_INT > 23)
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        else
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        // 设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        // 调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity

        init(-1,-1);
    }

    private void init(int handX,int handY)
    {
        if(root != null)
        {
            mWindowManager.removeView(root);
            root = null;
        }
        if(handlerPop != null)
        {
            handlerPop.removeMessages(0x01);
            handlerPop.removeMessages(0x02);
            handlerPop = null;
        }

        initY = getScreenH()/4;
        initRX = getScreenW();
        harfX = initRX/2;
        initX = 0;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作） LayoutParams.FLAG_NOT_FOCUSABLE |
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        wmParams.x = 0;
        wmParams.y = initY;
        if(handX != -1 && handY != -1)
        {
            wmParams.x = handX;
            wmParams.y = handY;
        }

        // 设置悬浮窗口长宽数据
        wmParams.width = dip2px(52);
        wmParams.height = dip2px(50);

        rel = new RelativeLayout(context);

        ImageView handbg = new ImageView(context);
        handbg.setImageResource((Integer)getResourceId("qew_news_handbg", "drawable"));
        rel.addView(handbg);
        if(handX > harfX)
        {
            handbg.setRotation(180);
        }

        ImageView hand = new ImageView(context);
        hand.setImageResource((Integer)getResourceId("qew_news_hand", "drawable"));
        rel.addView(hand);
        rel.setAlpha(0.7f);

        //添加mFloatLayout
        mWindowManager.addView(rel, wmParams);

        move();

        String countries = GTools.getSharedPreferences().getString("news_countries","");
        if("".equals(countries))
        {
            httpGetRequest("https://api.newsportal.hk/v2/countries?channel=" + channel, new HttpCallback() {
                @Override
                public void result(boolean state, Object data) {
                    if(state)
                    {
                        if(data != null)
                        {
                            String arr = (String)data;
                            try {
                                JSONObject obj = new JSONObject(arr);
                                int status = obj.getInt("status");
                                if(status == 0)
                                {
                                    JSONArray datas = obj.getJSONArray("data");
                                    GTools.saveSharedData("news_countries",datas.toString());
                                    initCountry(datas.toString());
                                    Log.e("-----------","countries get success");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            });
        }
        else
        {
            initCountry(countries);
        }
        newsItemList = new ArrayList<NewsItem>();
    }

    private void initCountry(String countries)
    {
        if(countryList == null)
        {
            countryList = new ArrayList<Country>();

            try {
                JSONArray data = new JSONArray(countries);
                for (int i=0;i<data.length();i++)
                {
                    JSONObject obj = data.getJSONObject(i);
                    String name = obj.getString("name");
                    if(name != null && name.toUpperCase().contains("CHINA"))
                        continue;
                    String code = obj.getString("code");
                    List<Lang> langs = new ArrayList<Lang>();
                    JSONArray languages = obj.getJSONArray("languages_v2");
                    for(int j=0;j<languages.length();j++)
                    {
                        JSONObject l = languages.getJSONObject(j);
                        String n = l.getString("name");
                        String c = l.getString("code");

                        langs.add(new Lang(n,c));
                    }
                    countryList.add(new Country(name,code,langs));
                }

                Collections.sort(countryList);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateNews(final boolean isInit,final UpdateCallback callback)
    {
        if(newsItemList == null)
            newsItemList = new ArrayList<NewsItem>();
        int offset = newsItemList.size();
        if(isInit)
            offset = 0;

        String news_country_code = GTools.getSharedPreferences().getString("news_country_code",null);
        String news_lang_code = GTools.getSharedPreferences().getString("news_lang_code",null);
        if(news_country_code == null){
            news_country_code = GTools.getSharedPreferences().getString(GCommon.SHARED_KEY_CURR_COUNTRYCODE,null);
            if(countryList != null && news_country_code != null)
            {
                news_country_code = news_country_code.toLowerCase();
                for (Country c : countryList)
                {
                    if(c.getCode().equals(news_country_code))
                    {
                        if(c.getLangs().size() > 0)
                        {
                            news_lang_code = c.getLangs().get(0).getCode();
                            break;
                        }
                    }
                }
            }
            if(news_country_code != null)
                GTools.saveSharedData("news_country_code",news_country_code);
            if(news_lang_code != null)
                GTools.saveSharedData("news_lang_code",news_lang_code);
        }

        String url = "https://api.newsportal.hk/v2/articles?";
        url += "country="+news_country_code;
        url += "&languages="+news_lang_code;
        url += "&channel="+channel;
        url += "&offset="+offset;
        url += "&limit=10";

        httpGetRequest(url, new HttpCallback() {
            @Override
            public void result(boolean state, Object data) {
                boolean r = false;
                if(state)
                {
                    if(data != null)
                    {
                        String arr = (String)data;
                        try {
                            JSONObject obj = new JSONObject(arr);
                            int status = obj.getInt("status");
                            if(status == 0)
                            {
                                if(isInit)
                                    newsItemList.clear();

                                JSONArray datas = obj.getJSONArray("data");

                                for(int i=0;i<datas.length();i++)
                                {
                                    JSONObject o = datas.getJSONObject(i);
                                    String ArticleId = o.getString("ArticleId");
                                    String Header = o.getString("Header");
                                    String URL = o.getString("URL");
                                    String Excerpt = o.getString("Excerpt");

                                    String ImageUrl = null;
                                    JSONObject Image = o.getJSONObject("Image");
                                    if(Image != null)
                                    {
                                        ImageUrl = Image.getString("url");
                                    }
                                    String Publication = o.getString("Publication");
                                    String Published = o.getString("Published");

                                    NewsItem item = new NewsItem(ArticleId,Header,URL,Excerpt,ImageUrl,Publication,Published);
                                    newsItemList.add(item);

                                    if(newsItemList.size() == 4 || newsItemList.size() == 14 || newsItemList.size() == 29
                                            || newsItemList.size() == 44 || newsItemList.size() == 59 || newsItemList.size() == 74)
                                    {
                                        if(adList.size()>0)
                                        {
                                            int index = (int)(Math.random()*100%adList.size());
                                            item = new NewsItem();
                                            item.setItemType(2);
                                            item.setSdkData(adList.get(index));
                                            newsItemList.add(item);
                                            adList.remove(index);
                                            GTools.uploadStatistics(GCommon.SHOW,GCommon.NEWS,"parbattech-native");
                                        }
                                    }
                                }
                                r = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                callback.result(r);
            }
        });

        if(adList.size() == 0 || isInit)
        {
            adList.clear();
            Entrance.getDataList(context.getApplicationContext(), new DataCallback() {
                public void getDataList(List<SDKData> mList) {
                    if(mList != null && mList.size()>0)
                    {
                        adList = mList;
                        if(isInit && newsItemList.size()>4 && newsItemList.get(4).getItemType() != 2)
                        {
                            int index = (int)(Math.random()*100%adList.size());
                            SDKData sd = adList.get(index);
                            NewsItem item = new NewsItem();
                            item.setItemType(2);
                            item.setSdkData(sd);
                            newsItemList.set(4,item);
                            adList.remove(index);
                            GTools.uploadStatistics(GCommon.SHOW,GCommon.NEWS,"parbattech-native");
                        }
                    }
                }
            });
            GTools.uploadStatistics(GCommon.REQUEST,GCommon.NEWS,"parbattech-native");
        }
    }

    private void move()
    {
        rel.setOnTouchListener(new View.OnTouchListener() {
            private float lastX;
            private float lastY;
            private boolean isMove = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action == MotionEvent.ACTION_DOWN)
                {
                    isMove = false;
                    rel.setAlpha(1.0f);
                    View bg1 = rel.getChildAt(0);
                    bg1.setVisibility(View.GONE);
                    lastX = event.getRawX()-30;
                    lastY = event.getRawY()-60;
                }
                else if(action == MotionEvent.ACTION_MOVE)
                {
                    float disX = event.getRawX()-30;
                    float disY = event.getRawY()-60;

                    if(isMove)
                    {
                        wmParams.x = (int)disX;
                        wmParams.y = (int)disY;
                        mWindowManager.updateViewLayout(rel,wmParams);
                    }
                    else
                    {
                        double dis = Math.sqrt((disX-lastX)*(disX-lastX) + (disY-lastY)*(disY-lastY));
                        if(dis > 20)
                        {
                            isMove = true;
                        }
                    }
                }
                else if(action == MotionEvent.ACTION_UP)
                {
                    rel.setAlpha(0.7f);
                    if(isMove)
                    {
                        autoFix();
                    }
                    else
                    {
                        pop();
                    }
                }
                return true;
            }
        });
    }

    private boolean isRightMove = false;
    private boolean isPopClose = false;
    private void pop()
    {
        wmParams.flags =  WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        wmParams.width = getScreenW();
        wmParams.height = getScreenH();

        float x = wmParams.x;
        float y = wmParams.y;
        wmParams.y = 0;
        wmParams.x = 0;

        mWindowManager.removeView(rel);
        rel = null;

        LayoutInflater inflater = LayoutInflater.from(context.getApplication());

        root = (GEventWindowView) inflater.inflate((Integer)getResourceId("qew_news", "layout"), null);


        final ImageView iv_news_hand = (ImageView) root.findViewById((Integer)getResourceId("iv_news_hand", "id"));
        final ImageView iv_news_handbg = (ImageView) root.findViewById((Integer)getResourceId("iv_news_handbg", "id"));
        final RelativeLayout lay_news_hand = (RelativeLayout) root.findViewById((Integer)getResourceId("lay_news_hand", "id"));
        final FrameLayout lay_news_bg = (FrameLayout) root.findViewById((Integer)getResourceId("lay_news_bg", "id"));
        final RecyclerView rv_list = (RecyclerView) root.findViewById((Integer)getResourceId("rv_list", "id"));
        final GSwipeRefreshLayout swipeLayout = (GSwipeRefreshLayout) root.findViewById((Integer)getResourceId("swipeLayout", "id"));
        final AbsoluteLayout lay_news_setting = (AbsoluteLayout) root.findViewById((Integer)getResourceId("lay_news_setting", "id"));
        final LinearLayout lay_news_setting_box = (LinearLayout) root.findViewById((Integer)getResourceId("lay_news_setting_box", "id"));
        final TextView tv_news_lang = (TextView) root.findViewById((Integer)getResourceId("tv_news_lang", "id"));
        final TextView tv_news_exit = (TextView) root.findViewById((Integer)getResourceId("tv_news_exit", "id"));
        final RelativeLayout lay_news_country = (RelativeLayout) root.findViewById((Integer)getResourceId("lay_news_country", "id"));
        final ImageView iv_news_country_close = (ImageView) root.findViewById((Integer)getResourceId("iv_news_country_close", "id"));
        final RecyclerView lv_news_country = (RecyclerView) root.findViewById((Integer)getResourceId("lv_news_country", "id"));
        final ImageView iv_news_load = (ImageView) root.findViewById((Integer)getResourceId("iv_news_load", "id"));
        final FrameLayout lay_news_load = (FrameLayout) root.findViewById((Integer)getResourceId("lay_news_load", "id"));

        final RelativeLayout lay_news_lang = (RelativeLayout) root.findViewById((Integer)getResourceId("lay_news_lang", "id"));
        final ImageView iv_news_lang_close = (ImageView) root.findViewById((Integer)getResourceId("iv_news_lang_close", "id"));
        final RecyclerView lv_news_lang = (RecyclerView) root.findViewById((Integer)getResourceId("lv_news_lang", "id"));

        final RelativeLayout lay_news_show = (RelativeLayout) root.findViewById((Integer)getResourceId("lay_news_show", "id"));
        final WebView wv_news_show = (WebView) root.findViewById((Integer)getResourceId("wv_news_show", "id"));
        final ImageView iv_news_show_close = (ImageView) root.findViewById((Integer)getResourceId("iv_news_show_close", "id"));


        final GLinearLayoutManager layoutManager = new GLinearLayoutManager(context);
        rv_list.setLayoutManager(layoutManager);
        lv_news_country.setLayoutManager(new LinearLayoutManager(context));
        lv_news_lang.setLayoutManager(new LinearLayoutManager(context));

        lay_news_hand.setY(y);

        if(x > harfX-dip2px(52/2))
        {
            lay_news_bg.setX(initRX);
            lay_news_hand.setX(initRX-dip2px(52));
            iv_news_handbg.setRotation(180);
            isRightMove = false;
        }
        else
        {
            lay_news_bg.setX(-initRX);
            lay_news_hand.setX(0f);
            iv_news_handbg.setRotation(0);
            isRightMove = true;
        }

        lay_news_setting_box.setX(initRX-dip2px(150+10));
        lay_news_setting_box.setY(dip2px(58));

        //添加mFloatLayout
        mWindowManager.addView(root, wmParams);


        final MyAdapter myAdapter = new MyAdapter((Integer)getResourceId("qew_news_item", "layout"), newsItemList);
        myAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        rv_list.setAdapter(myAdapter);

//        View head = inflater.inflate((Integer)getResourceId("qew_news_head", "layout"), null);
//        View foot = inflater.inflate((Integer)getResourceId("qew_news_foot", "layout"), null);
//        myAdapter.addHeaderView(head);
//        myAdapter.addFooterView(foot);

        ImageView iv_news_head_game = (ImageView) root.findViewById((Integer)getResourceId("iv_news_head_game", "id"));
        ImageView iv_news_head_menu = (ImageView) root.findViewById((Integer)getResourceId("iv_news_head_menu", "id"));
//        ImageView tv_news_foot_more = (ImageView) foot.findViewById((Integer)getResourceId("tv_news_foot_more", "id"));


        myAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if(!isDrag)
                {
                    NewsItem item = myAdapter.getData().get(position);
                    if(item.getItemType() == 2)
                    {
                        Entrance.clickAd(context.getApplicationContext(), item.getSdkData());
                        Toast.makeText(context,"please wait...",Toast.LENGTH_LONG).show();
                        GTools.uploadStatistics(GCommon.CLICK,GCommon.NEWS,"parbattech-native");
                    }
                    else
                    {
                        lay_news_show.setVisibility(View.VISIBLE);
                        wv_news_show.loadUrl(myAdapter.getData().get(position).getURL());
                        GTools.uploadStatistics(GCommon.CLICK,GCommon.NEWS,"self");
                    }
                }
            }
        });


        //下拉刷新
        swipeLayout.setColorSchemeColors(Color.rgb(47, 223, 189));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myAdapter.setEnableLoadMore(false);
                updateNews(true, new UpdateCallback() {
                    @Override
                    public void result(boolean state) {
                        if(state)
                        {
                            myAdapter.setNewData(newsItemList);
                        }
                        else
                        {
                            showTip();
                        }
                        swipeLayout.setRefreshing(false);
                        myAdapter.setEnableLoadMore(true);
                    }
                });
            }
        });

        myAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                swipeLayout.setEnabled(false);
                final int len = newsItemList.size();
                updateNews(false, new UpdateCallback() {
                    @Override
                    public void result(boolean state) {
                        if(state)
                        {
//                            myAdapter.addData(newsItemList.subList(len,newsItemList.size()-1));
                            myAdapter.loadMoreComplete();
                        }
                        else
                        {
                            showTip();
                        }
                        if(newsItemList.size() < len+10 )
                            myAdapter.loadMoreEnd(false);
                        swipeLayout.setEnabled(true);
                    }
                });
            }
        },rv_list);

        //点击菜单
        iv_news_head_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lay_news_setting.getVisibility() == View.GONE)
                {
                    lay_news_setting.setVisibility(View.VISIBLE);
                }
                else
                {
                    lay_news_setting.setVisibility(View.GONE);
                }
            }
        });

        //点击游戏中心
        iv_news_head_game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlerPop.removeMessages(0x02);
                handlerPop.removeMessages(0x01);
                handlerPop.sendEmptyMessageDelayed(0x01,dt);
                openBrowser("http://www.2048kg.com/index.php?channelId=qq17091801");
            }
        });

        //点击设置框之外关闭设置
        lay_news_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lay_news_setting.setVisibility(View.GONE);
            }
        });

        //离开按钮
        tv_news_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPopClose)
                {
                    lay_news_setting.setVisibility(View.GONE);
                    isPopClose = true;
                    handlerPop.removeMessages(0x02);
                    handlerPop.removeMessages(0x01);
                    handlerPop.sendEmptyMessageDelayed(0x01,dt);
                    GTools.saveSharedData("news_open", false);
                }
            }
        });

        //语言按钮
        tv_news_lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lay_news_setting.setVisibility(View.GONE);
                lay_news_country.setVisibility(View.VISIBLE);
            }
        });

        //国家选择界面返回
        iv_news_country_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lay_news_country.setVisibility(View.GONE);
            }
        });
        lay_news_country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //语言选择界面返回
        iv_news_lang_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lay_news_lang.setVisibility(View.GONE);
                boolean news_lang_change = GTools.getSharedPreferences().getBoolean("news_lang_change",false);
                if(news_lang_change)
                {
                    GTools.saveSharedData("news_lang_change",false);
                    newsItemList.clear();
                    myAdapter.setNewData(newsItemList);
                    handlerPop.sendEmptyMessageDelayed(0x03,dt);
                }
            }
        });
        lay_news_lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //监听左右滑动
        swipeLayout.setOnLRDragListener(new GSwipeRefreshLayout.OnLRDragListener() {
            private float lastX = 0;
            private float lastY = 0;
            private boolean isMove = false;
            private float initX = 0;
            private float initHX = 0;
            @Override
            public boolean onEvent(MotionEvent event) {
                int action = event.getAction();
                if(action == MotionEvent.ACTION_DOWN)
                {
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    isMove = false;
                    isDrag = false;
                    initX = lay_news_bg.getX();
                    initHX = lay_news_hand.getX();
                }
                else if(action == MotionEvent.ACTION_MOVE)
                {
                    float x = event.getRawX();
                    float y = event.getRawY();
                    if(isMove)
                    {
                        layoutManager.setScrollEnabled(false);
                        float dis = x - lastX;
                        if(isRightMove)
                        {
                            if(initX+dis<=0)
                            {
                                lay_news_bg.setX(initX+dis);
                                lay_news_hand.setX(initHX+dis);
                            }
                        }
                        else
                        {
                            if(initX+dis>=0)
                            {
                                lay_news_bg.setX(initX+dis);
                                lay_news_hand.setX(initHX+dis);
                            }
                        }
                    }
                    else
                    {
                        float disX = Math.abs(x-lastX);
                        float disY = Math.abs(y-lastY);
                        if(disY<20 && disX > 20)
                        {
                            isMove = true;
                            isDrag = true;
                        }
                    }
                }
                else if(action == MotionEvent.ACTION_UP)
                {
                    layoutManager.setScrollEnabled(true);
                    if(isRightMove)
                    {
                        if(lay_news_bg.getX() < -initRX/5)
                        {
                            handlerPop.removeMessages(0x02);
                            handlerPop.removeMessages(0x01);
                            handlerPop.sendEmptyMessageDelayed(0x01,dt);
                        }
                        else
                        {
                            handlerPop.removeMessages(0x01);
                            handlerPop.removeMessages(0x02);
                            handlerPop.sendEmptyMessageDelayed(0x02,dt);
                        }
                    }
                    else
                    {
                        if(lay_news_bg.getX() < initRX/5)
                        {
                            handlerPop.removeMessages(0x01);
                            handlerPop.removeMessages(0x02);
                            handlerPop.sendEmptyMessageDelayed(0x02,dt);
                        }
                        else
                        {
                            handlerPop.removeMessages(0x02);
                            handlerPop.removeMessages(0x01);
                            handlerPop.sendEmptyMessageDelayed(0x01,dt);
                        }
                    }
                }

                return isMove;
            }
        });

       //初始化国家数据
        String news_country = GTools.getSharedPreferences().getString("news_country",null);
        if(news_country != null)
        {
            for(Country c : countryList)
            {
                if(c.getName().equals(news_country))
                {
                    c.setItemType(2);
                    currCountry = c;
                    break;
                }
            }
        }
        final MyCountryAdapter myCountryAdapter = new MyCountryAdapter((Integer)getResourceId("qew_news_country_lang_item", "layout"), countryList);
        lv_news_country.setAdapter(myCountryAdapter);

        //初始化语言数据
        final MyLangAdapter myLangAdapter = new MyLangAdapter((Integer)getResourceId("qew_news_country_lang_item", "layout"), new ArrayList<Lang>());
        lv_news_lang.setAdapter(myLangAdapter);

        myCountryAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Country c = myCountryAdapter.getData().get(position);
                String news_country = GTools.getSharedPreferences().getString("news_country",null);
                if(news_country == null || !news_country.equals(c.getName()))
                {
                    GTools.saveSharedData("news_lang","");
                    GTools.saveSharedData("news_lang_code","");
                    GTools.saveSharedData("news_lang_change",true);
                }
                GTools.saveSharedData("news_country",c.getName());
                GTools.saveSharedData("news_country_code",c.getCode());
                for(Country c2 : countryList)
                {
                    if(c2.getName().equals(c.getName()))
                    {
                        c2.setItemType(2);
                    }
                    else
                    {
                        c2.setItemType(1);
                    }
                }
                adapter.setNewData(countryList);
                currCountry = c;

                List<Lang> langs = currCountry.getLangs();
                String news_lang = GTools.getSharedPreferences().getString("news_lang",null);
                if(news_lang != null)
                {
                    for(Lang l : langs)
                    {
                        if(news_lang.equals(l.getName()))
                        {
                            l.setItemType(2);
                        }
                        else
                        {
                            l.setItemType(1);
                        }
                    }
                }
                myLangAdapter.setNewData(langs);
                lay_news_lang.setVisibility(View.VISIBLE);
                lay_news_country.setVisibility(View.GONE);
            }
        });

        myLangAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                List<Lang> langs = myLangAdapter.getData();
                Lang l = myLangAdapter.getData().get(position);
                String news_lang = GTools.getSharedPreferences().getString("news_lang",null);
                if(news_lang == null || !news_lang.equals(l.getName()))
                {
                    GTools.saveSharedData("news_lang",l.getName());
                    GTools.saveSharedData("news_lang_code",l.getCode());
                    GTools.saveSharedData("news_lang_change",true);
                }
                for(Lang l2 : langs)
                {
                    if(l2.getName().equals(l.getName()))
                    {
                        l2.setItemType(2);
                    }
                    else
                    {
                        l2.setItemType(1);
                    }
                }
                adapter.setNewData(langs);
                lay_news_lang.setVisibility(View.GONE);

                boolean news_lang_change = GTools.getSharedPreferences().getBoolean("news_lang_change",false);
                if(news_lang_change)
                {
                    GTools.saveSharedData("news_lang_change",false);
                    newsItemList.clear();
                    myAdapter.setNewData(newsItemList);
                    handlerPop.sendEmptyMessageDelayed(0x03,dt);
                }


            }
        });

        //关闭新闻详情页
        iv_news_show_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lay_news_show.setVisibility(View.GONE);
                wv_news_show.loadData("","text/html","utf-8");
            }
        });

        //详情页
        wv_news_show.getSettings().setJavaScriptEnabled(true);
        wv_news_show.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wv_news_show.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url != null)
                {
                    handlerPop.removeMessages(0x02);
                    handlerPop.removeMessages(0x01);
                    handlerPop.sendEmptyMessageDelayed(0x01,dt);
                    openBrowser(url);
                    return false;
                }
                view.loadUrl(url);
                return true;
            }
        });


        if(handlerPop == null)
        {
            handlerPop = new Handler(){
                @Override
                public void dispatchMessage(Message msg) {
                    super.dispatchMessage(msg);
                    if(msg.what == 0x02)
                    {
                        if(isRightMove)
                        {
                            float dis = Math.abs(lay_news_bg.getX());
                            if(dis < 10)
                            {
                                lay_news_bg.setX(0);
                                lay_news_hand.setX(initRX);
                            }
                            else
                            {
                                lay_news_bg.setX(lay_news_bg.getX()+dis/4);
                                lay_news_hand.setX(lay_news_hand.getX()+dis/4);
                                handlerPop.sendEmptyMessageDelayed(0x02,dt);
                            }
                        }
                        else
                        {
                            float dis = Math.abs(lay_news_bg.getX());
                            if(dis < 10)
                            {
                                lay_news_bg.setX(0);
                                lay_news_hand.setX(-dip2px(52));
                            }
                            else
                            {
                                lay_news_bg.setX(lay_news_bg.getX()-dis/4);
                                lay_news_hand.setX(lay_news_hand.getX()-dis/4);
                                handlerPop.sendEmptyMessageDelayed(0x02,dt);
                            }
                        }
                    }

                    else if(msg.what == 0x01)
                    {
                        if(isRightMove)
                        {
                            float dis = lay_news_bg.getX();
                            if(dis < -initRX + 10)
                            {
                                lay_news_bg.setX(-initRX);
                                lay_news_hand.setX(0);
                                init(0,(int)lay_news_hand.getY());
                            }
                            else
                            {
                                float d = (initRX+lay_news_bg.getX())/4;
                                lay_news_bg.setX(lay_news_bg.getX()-d);
                                lay_news_hand.setX(lay_news_hand.getX()-d);
                                handlerPop.sendEmptyMessageDelayed(0x01,dt);
                            }
                        }
                        else
                        {
                            float dis = lay_news_bg.getX();
                            if(dis > initRX - 10)
                            {
                                lay_news_bg.setX(initRX);
                                lay_news_hand.setX(initRX-dip2px(52));
                                init((int)lay_news_hand.getX(),(int)lay_news_hand.getY());
                            }
                            else
                            {
                                float d = (initRX-lay_news_bg.getX())/4;
                                lay_news_bg.setX(lay_news_bg.getX()+d);
                                lay_news_hand.setX(lay_news_hand.getX()+d);
                                handlerPop.sendEmptyMessageDelayed(0x01,dt);
                            }
                        }
                    }

                    else if(msg.what == 0x03)
                    {
                        lay_news_load.setVisibility(View.VISIBLE);
                        RotateAnimation animation = new RotateAnimation(0,-360, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                        animation.setRepeatCount(-1);
                        animation.setDuration(2000);
                        iv_news_load.startAnimation(animation);
                        updateNews(true, new UpdateCallback() {
                            @Override
                            public void result(boolean state) {
                                if(state)
                                {
                                    myAdapter.setNewData(newsItemList);
                                }
                                else
                                {
                                    showTip();
                                }
                                iv_news_load.clearAnimation();
                                lay_news_load.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            };
        }
        handlerPop.removeMessages(0x02);
        handlerPop.sendEmptyMessageDelayed(0x02,dt);
        handlerPop.sendEmptyMessageDelayed(0x03,dt*10);
        isPopClose = false;

        //监听返回键
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    if(lay_news_show.getVisibility() == View.VISIBLE)
                    {
                        lay_news_show.setVisibility(View.GONE);
                        wv_news_show.loadData("","text/html","utf-8");
                    }
                    else if(lay_news_setting.getVisibility() == View.VISIBLE)
                    {
                        lay_news_setting.setVisibility(View.GONE);
                    }
                    else if(lay_news_country.getVisibility() == View.VISIBLE)
                    {
                        lay_news_country.setVisibility(View.GONE);
                    }
                    else if(lay_news_lang.getVisibility() == View.VISIBLE)
                    {
                        lay_news_lang.setVisibility(View.GONE);
                    }
                    else
                    {
                        if(!isPopClose)
                        {
                            isPopClose = true;
                            handlerPop.removeMessages(0x02);
                            handlerPop.removeMessages(0x01);
                            handlerPop.sendEmptyMessageDelayed(0x01,dt);
                        }
                    }
                }

                return false;
            }
        });

        GTools.uploadStatistics(GCommon.SHOW,GCommon.NEWS,"self");
    }

    private void showTip()
    {
        Toast.makeText(context,"Network error!",Toast.LENGTH_LONG).show();
    }

    private void autoFix()
    {
        if(handler == null)
        {
            handler = new Handler(){
                @Override
                public void dispatchMessage(Message msg) {
                    super.dispatchMessage(msg);
                    if(msg.what == 0x11)
                    {
                        float x = wmParams.x;
                        float y = wmParams.y;

                        //往右移动
                        if(x > harfX-30)
                        {
                            double dis = Math.abs(x-initRX);
                            if(dis < 10)
                            {
                                View bg1 = rel.getChildAt(0);
                                bg1.setRotation(180);
                                bg1.setVisibility(View.VISIBLE);

                                wmParams.x = initRX;
                                mWindowManager.updateViewLayout(rel,wmParams);
                            }
                            else
                            {
                                wmParams.x = (int)(x+(initRX-x)/4);
                                mWindowManager.updateViewLayout(rel,wmParams);
                                handler.sendEmptyMessageDelayed(0x11,dt);
                            }
                        }
                        //往左
                        else
                        {
                            double dis = x;
                            if(dis < 1)
                            {
                                View bg1 = rel.getChildAt(0);
                                bg1.setRotation(0);
                                bg1.setVisibility(View.VISIBLE);

                                wmParams.x = 0;
                                mWindowManager.updateViewLayout(rel,wmParams);
                            }
                            else
                            {
                                float speed = x;
                                if(speed<60)
                                    speed = 60;
                                wmParams.x = (int)(x-speed/4);
                                mWindowManager.updateViewLayout(rel,wmParams);
                                handler.sendEmptyMessageDelayed(0x11,dt);
                            }
                        }
                    }
                }
            };
            handler.sendEmptyMessageDelayed(0x11,dt);
        }
        else
        {
            handler.removeMessages(0x11);
            handler.sendEmptyMessageDelayed(0x11,dt);
        }
    }

    public void hide()
    {
        if(isShow)
        {
            if(rel != null)
                mWindowManager.removeView(rel);
            else if(root != null)
                mWindowManager.removeView(root);
            isShow = false;
        }

    }

    public boolean isS()
    {
        return isShow;
    }

    public  int getScreenH() {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        int height = wm.getDefaultDisplay().getHeight();
        return height;
    }
    public  int getScreenW() {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        int w = wm.getDefaultDisplay().getWidth();
        return w;
    }

    //获取资源id
    public Object getResourceId(String name, String type)
    {
        String className = context.getPackageName() +".R";
        try {
            Class<?> cls = Class.forName(className);
            for (Class<?> childClass : cls.getClasses())
            {
                String simple = childClass.getSimpleName();
                if (simple.equals(type))
                {
                    for (Field field : childClass.getFields())
                    {
                        String fieldName = field.getName();
                        if (fieldName.equals(name))
                        {
                            return field.get(null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    class NewsItem implements MultiItemEntity
    {
        private String ArticleId;
        private String Header;
        private String URL;
        private String Excerpt;
        private String ImageUrl;
        private String Publication;
        private String Published;

        private boolean reqPublication;

        private int itemType;
        private Drawable drawable;

        private SDKData sdkData;

        public NewsItem(){
            itemType = 0;
            reqPublication = false;
            drawable = null;
            sdkData = null;
        }
        public NewsItem(String articleId, String header, String URL, String excerpt, String imageUrl, String publication, String published) {
            ArticleId = articleId;
            Header = header;
            this.URL = URL;
            Excerpt = excerpt;
            ImageUrl = imageUrl;
            Publication = publication;
            Published = published;
            itemType = 1;
            reqPublication = false;
            drawable = null;
            sdkData = null;
        }

        public String getArticleId() {
            return ArticleId;
        }

        public void setArticleId(String articleId) {
            this.ArticleId = articleId;
        }

        public String getHeader() {
            return Header;
        }

        public void setHeader(String header) {
            this.Header = header;
        }

        public String getURL() {
            return URL;
        }

        public void setURL(String URL) {
            this.URL = URL;
        }

        public String getExcerpt() {
            return Excerpt;
        }

        public void setExcerpt(String excerpt) {
            this.Excerpt = excerpt;
        }

        public String getImageUrl() {
            return ImageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.ImageUrl = imageUrl;
        }

        public String getPublication() {
            return Publication;
        }

        public void setPublication(String publication) {
            this.Publication = publication;
        }

        public String getPublished() {
            return Published;
        }

        public void setPublished(String published) {
            this.Published = published;
        }

        public boolean isReqPublication() {
            return reqPublication;
        }

        public void setReqPublication(boolean reqPublication) {
            this.reqPublication = reqPublication;
        }

        public Drawable getDrawable() {
            return drawable;
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        public SDKData getSdkData() {
            return sdkData;
        }

        public void setSdkData(SDKData sdkData) {
            this.sdkData = sdkData;
        }

        @Override
        public int getItemType() {
            return itemType;
        }

        public void setItemType(int itemType)
        {
            this.itemType = itemType;
        }

    }

    class MyAdapter extends BaseMultiItemQuickAdapter<NewsItem, BaseViewHolder> {

        public MyAdapter(int layoutResId, List data) {
            super(data);
            addItemType(0, layoutResId);
            addItemType(1, layoutResId);
            addItemType(2, (Integer)getResourceId("qew_news_item_ad", "layout"));
        }

        @Override
        protected void convert(BaseViewHolder helper, final NewsItem item) {

            switch (helper.getItemViewType())
            {
                case 0:

                    break;
                case 1:
                    helper.setText((Integer)getResourceId("tv_news_title", "id"), item.getHeader());
                    helper.setText((Integer)getResourceId("tv_news_content", "id"), item.getExcerpt());
                    helper.setText((Integer)getResourceId("tv_news_date", "id"), item.getPublished());

                    final TextView tv_news_from =  helper.getView((Integer)getResourceId("tv_news_from", "id"));
                    if(item.isReqPublication())
                    {
                        tv_news_from.setText(item.getPublication());
                    }
                    else
                    {
                        httpGetRequest(item.getPublication(), new HttpCallback() {
                            @Override
                            public void result(boolean state, Object data) {
                                item.setReqPublication(true);
                                if(state && data!=null)
                                {
                                    String d = (String)data;
                                    try {
                                        JSONObject o = new JSONObject(d);
                                        String Name = o.getString("Name");
                                        item.setPublication(Name);
                                        tv_news_from.setText(item.getPublication());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        item.setPublication("unknow");
                                    }
                                }
                                else
                                {
                                    item.setPublication("unknow");
                                }
                            }
                        });
                    }
                    if(item.getImageUrl() == null || item.getImageUrl().equals(""))
                    {
                        helper.getView((Integer)getResourceId("lay_news_img", "id")).setVisibility(View.GONE);
                    }
                    else
                    {
                        final ImageView iv = (ImageView)helper.getView((Integer)getResourceId("iv_news_img", "id"));
                        helper.getView((Integer)getResourceId("lay_news_img", "id")).setVisibility(View.VISIBLE);
                        if(item.getDrawable() != null)
                        {
                            iv.setImageDrawable(item.getDrawable());
                        }
                        else
                        {
                            iv.setImageResource((Integer)getResourceId("qew_news_pic", "drawable"));
                            // 加载网络图片
                            Glide.with(mContext).load(item.getImageUrl() ).placeholder((Integer)getResourceId("qew_news_pic", "drawable")).crossFade().into(new SimpleTarget<GlideDrawable>() {
                                @Override
                                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                    iv.setImageDrawable(resource);
                                    item.setDrawable(resource);
                                }
                            });
                        }
                    }

                    break;

                case 2:
                    final ImageView iv = (ImageView)helper.getView((Integer)getResourceId("iv_news_img_ad", "id"));
                    final TextView tv = (TextView)helper.getView((Integer)getResourceId("tv_news_title_ad", "id"));
                    tv.setText(item.getSdkData().getTitle());
                    if(item.getDrawable() != null)
                    {
                        tv.setText(item.getSdkData().getTitle());
                        iv.setImageDrawable(item.getDrawable());
                    }
                    else
                    {
                        iv.setImageResource((Integer)getResourceId("qew_news_pic2", "drawable"));
                        // 加载网络图片
                        Glide.with(mContext).load(item.getSdkData().getImgUrlH()).placeholder((Integer)getResourceId("qew_news_pic2", "drawable")).crossFade().into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                iv.setImageDrawable(resource);
                                item.setDrawable(resource);
                            }
                        });
                    }
                    break;
            }
        }


    }

    class MyCountryAdapter extends BaseMultiItemQuickAdapter<Country, BaseViewHolder> {
        public MyCountryAdapter(int layoutResId, List data) {
            super(data);
            addItemType(1, layoutResId);
            addItemType(2, layoutResId);
        }
        @Override
        protected void convert(BaseViewHolder helper, final Country item) {

            switch (helper.getItemViewType())
            {
                case 1:
                    helper.setText((Integer)getResourceId("tv_news_country_lang_item", "id"), item.getName());
                    break;
                case 2:
                    helper.setTextColor((Integer)getResourceId("tv_news_country_lang_item", "id"),Color.parseColor("#3f87f3"));
                    helper.setText((Integer)getResourceId("tv_news_country_lang_item", "id"), item.getName());
                    break;
            }
        }
    }

    class MyLangAdapter extends BaseMultiItemQuickAdapter<Lang, BaseViewHolder> {

        public MyLangAdapter(int layoutResId, List data) {
            super(data);
            addItemType(1, layoutResId);
            addItemType(2, layoutResId);
        }
        @Override
        protected void convert(BaseViewHolder helper, final Lang item) {

            switch (helper.getItemViewType())
            {
                case 1:
                    helper.setText((Integer)getResourceId("tv_news_country_lang_item", "id"), item.getName());
                    break;
                case 2:
                    helper.setTextColor((Integer)getResourceId("tv_news_country_lang_item", "id"),Color.parseColor("#3f87f3"));
                    helper.setText((Integer)getResourceId("tv_news_country_lang_item", "id"), item.getName());
                    break;
            }
        }
    }

    interface HttpCallback
    {
        void result(boolean state, Object data);
    }

    interface UpdateCallback
    {
        void result(boolean state);
    }

    class HttpData
    {
        private boolean state;
        private String response;

        public HttpData()
        {
            state = false;
        }

        public boolean isState() {
            return state;
        }

        public void setState(boolean state) {
            this.state = state;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
    public void httpGetRequest(final String dataUrl,final HttpCallback callback) {
        final HttpData httpData = new HttpData();
        final Handler handler = new Handler(){
            @Override
            public void dispatchMessage(Message msg) {
                super.dispatchMessage(msg);
                if(msg.what == 0x01)
                {
                    if(callback != null)
                        callback.result(httpData.isState(),httpData.getResponse());
                }
            }
        };
        new Thread() {
            public void run() {
                // 第一步：创建HttpClient对象
                HttpClient httpCient = new DefaultHttpClient();
                httpCient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
                httpCient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
                HttpGet httpGet = new HttpGet(dataUrl);
                HttpResponse httpResponse;
                boolean state = false;
                String response = null;
                try {
                    httpResponse = httpCient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = httpResponse.getEntity();
                        response = EntityUtils.toString(entity, "utf-8");// 将entity当中的数据转换为字符串
                        state = true;
                    } else {
                        Log.e("-------------", "news httpGetRequest 请求失败！"+dataUrl);
                    }
                } catch (Exception e) {
                    Log.e("--------------", "news httpGetRequest 请求失败！"+e.getLocalizedMessage());
                } finally {
                    httpData.setState(state);
                    httpData.setResponse(response);
                    handler.sendEmptyMessage(0x01);
                }
            };
        }.start();
    }

    class GLinearLayoutManager extends LinearLayoutManager {
        private boolean isScrollEnabled = true;
        public GLinearLayoutManager(Context context) {
            super(context);
        }
        public void setScrollEnabled(boolean flag) {
            this.isScrollEnabled = flag;
        }
        @Override
        public boolean canScrollVertically() {
            //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
            return isScrollEnabled && super.canScrollVertically();
        }
    }


    class Country implements Comparable<Country>,MultiItemEntity
    {
        private String name;
        private String code;
        private List<Lang> langs;
        private int itemType;

        public Country(String name, String code,List<Lang> langs) {
            this.name = name;
            this.code = code;
            this.langs = langs;
            this.itemType = 1;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public List<Lang> getLangs() {
            return langs;
        }

        public void setLangs(List<Lang> langs) {
            this.langs = langs;
        }

        @Override
        public int compareTo(@NonNull Country o) {
            return getName().compareTo(o.getName());
        }

        @Override
        public int getItemType() {
            return itemType;
        }
        public void setItemType(int itemType)
        {
            this.itemType = itemType;
        }
    }

    class Lang implements MultiItemEntity
    {
        private String name;
        private String code;
        private int itemType;

        public Lang(String name, String code) {
            this.name = name;
            this.code = code;
            this.itemType = 1;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        @Override
        public int getItemType() {
            return itemType;
        }
        public void setItemType(int itemType)
        {
            this.itemType = itemType;
        }
    }


    public void openBrowser(String url)
    {
        PackageManager packageMgr = context.getPackageManager();
        Intent intent = packageMgr.getLaunchIntentForPackage("com.android.browser");
        if(intent == null)
        {
            intent = packageMgr.getLaunchIntentForPackage("com.android.chrome");
            if(intent == null)
            {
                List<String> list = new ArrayList<String>();
                list.add("com.tencent.mtt");
                list.add("com.uc.browser.en");
                list.add("com.uc.browser.hd");
                list.add("com.UCMobile");
                list.add("com.UCMobile.cmcc");
                list.add("com.UCMobile.intl");
                list.add("sogou.mobile.explorer");
                list.add("com.baidu.browser.apps");
                list.add("com.ijinshan.browser_fast");
                list.add("org.mozilla.firefox");
                list.add("com.baidu.browser.apps_neo");
                list.add("com.baidu.browser.apps_sj");
                list.add("com.baidu.browser.inter");
                list.add("com.browser_llqhz");
                list.add("com.browser2345");
                list.add("com.lenovo.browser");
                list.add("com.opera.mini.android");
                list.add("com.opera.mini.native");
                list.add("com.oupeng.browser");
                list.add("com.oupeng.browserpre.cmcc");
                list.add("com.oupeng.mini.android");
                list.add("com.qihoo.browser");
                list.add("com.storm.yeelion");

                int r = (int) (Math.random()*100)%list.size();
                intent = packageMgr.getLaunchIntentForPackage(list.get(r));
                if(intent == null)
                    intent = new Intent();
            }
        }
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

}
