package com.paramnahata.nahatatv;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Original, dependency-free TV home. All navigation uses native focusable controls. */
public class MainActivity extends Activity {
    private LinearLayout page, grid;
    private TextView clock;
    private EditText search;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private android.content.SharedPreferences prefs;
    private final ArrayList<App> apps = new ArrayList<>();
    private int ink = Color.rgb(240,239,229), muted = Color.rgb(170,182,178), accent;
    private String filter = "";
    private boolean allApps=false;
    private boolean lightMode=false;
    private int selectedIndex=0;
    private View homeFocus;
    private android.graphics.Bitmap scenic;
    private boolean rearranging=false;
    private int scenicResource=0;
    private ArrayList<String> reorderDraft;
    private int movedFrom=-1,movedTo=-1;
    private android.animation.ValueAnimator ambient;
    private float ambientPhase=0f;
    private AuroraBackground backdrop;
    private void stopAmbient(){if(ambient!=null){ambient.cancel();ambient=null;}}
    private void startAmbient(){
        stopAmbient();if(prefs==null||backdrop==null||allApps||!motion()||!prefs.getBoolean("ambientMotion",true))return;
        ambient=android.animation.ValueAnimator.ofFloat(0f,1f);ambient.setDuration(60000);ambient.setRepeatCount(android.animation.ValueAnimator.INFINITE);ambient.setRepeatMode(android.animation.ValueAnimator.REVERSE);ambient.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        final long[] last={0};ambient.addUpdateListener(a->{long now=android.os.SystemClock.uptimeMillis();if(now-last[0]>=80){ambientPhase=(float)a.getAnimatedValue();backdrop.invalidateSelf();last[0]=now;}});ambient.start();
    }
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)startAmbient();else stopAmbient();}
    private final Runnable tick = new Runnable() { public void run() {
        if (clock != null) clock.setText(new SimpleDateFormat("EEE, dd MMM   •   HH:mm", Locale.getDefault()).format(new Date()));
        handler.postDelayed(this, 15000);
    }};
    static class App {
        String title, key; android.graphics.drawable.Drawable icon; Intent intent;
        App(String t, String k, android.graphics.drawable.Drawable d, Intent i) {title=t;key=k;icon=d;intent=i;}
    }
    @Override public void onCreate(Bundle state) {super.onCreate(state); prefs=getSharedPreferences("home",MODE_PRIVATE); loadApps(); render();if(state==null) entrance();}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);allApps=false;rearranging=false;filter="";render();}
    @Override public void onBackPressed(){if(rearranging){rearranging=false;reorderDraft=null;movedFrom=-1;drawApps();}else if(allApps){allApps=false;filter="";render();}else{if(homeFocus!=null)homeFocus.requestFocus();((ScrollView)page.getParent()).smoothScrollTo(0,0);}}
    @Override public void onResume(){super.onResume();prefs.edit().putBoolean("originalMode",false).apply();loadApps();if(grid!=null)drawApps();handler.removeCallbacks(tick);handler.post(tick);startAmbient();}
    @Override public void onPause(){handler.removeCallbacks(tick);stopAmbient();super.onPause();}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    private void entrance(){
        if(Settings.Global.getFloat(getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1f)==0f)return;
        // Animate the actual home, keeping every control available throughout.
        for(int i=0;i<page.getChildCount();i++){
            View child=page.getChildAt(i);child.setAlpha(0f);child.setTranslationY(dp(14));
            child.animate().alpha(1f).translationY(0f).setStartDelay(80L+Math.min(i,5)*45L).setDuration(320).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
        }
        final FrameLayout decor=(FrameLayout)getWindow().getDecorView().findViewById(android.R.id.content);
        final TextView mark=text("नाहटा",66,accent);mark.setTypeface(null,Typeface.BOLD);mark.setGravity(Gravity.CENTER);mark.setBackgroundColor(0xff0c211d);mark.setFocusable(false);mark.setClickable(false);mark.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        decor.addView(mark,new FrameLayout.LayoutParams(-1,-1));mark.setAlpha(0f);mark.setScaleX(.94f);mark.setScaleY(.94f);
        mark.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(180).withEndAction(()->mark.animate().alpha(0f).scaleX(1.035f).scaleY(1.035f).setStartDelay(80).setDuration(300).withEndAction(()->decor.removeView(mark)).start()).start();
    }
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private GradientDrawable bg(int color,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(16));if(stroke!=0)d.setStroke(dp(2),stroke);return d;}
    private void style(View v){v.setFocusable(true);v.setPadding(dp(16),dp(12),dp(16),dp(12));v.setBackground(bg(lightMode?0x80ffffff:0x14ffffff,lightMode?0x35a08c6a:0x25ffffff));v.setOnFocusChangeListener((w,f)->{w.setBackground(bg(f?(lightMode?0xe6ffffff:0x30ffffff):(lightMode?0x80ffffff:0x14ffffff),f?accent:(lightMode?0x35a08c6a:0x25ffffff)));w.setScaleX(f?1.025f:1);w.setScaleY(f?1.025f:1);});}
    private Button button(String title,Runnable action){Button b=new Button(this);b.setText(title);b.setTextColor(ink);b.setTextSize(15);b.setAllCaps(false);style(b);b.setOnClickListener(v->action.run());return b;}
    private void launch(Intent i){try{startActivity(i);}catch(Exception e){new PremiumDialog().setTitle("Unavailable on this TV").setMessage("This firmware does not expose that screen or app. Use the physical remote's Input or Settings button.").setPositiveButton("OK",null).show();}}
    private void loadApps(){
        apps.clear();PackageManager pm=getPackageManager();HashSet<String> seen=new HashSet<>();
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}){
            Intent q=new Intent(Intent.ACTION_MAIN).addCategory(category);
            for(ResolveInfo r:pm.queryIntentActivities(q,0)){
                String p=r.activityInfo.packageName;if(p.equals(getPackageName())||!seen.add(p))continue;
                Intent i=new Intent(Intent.ACTION_MAIN).addCategory(category).setComponent(new ComponentName(p,r.activityInfo.name)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                apps.add(new App(r.loadLabel(pm).toString(),p,r.loadIcon(pm),i));
            }
        }
        apps.sort((a,b)->a.title.compareToIgnoreCase(b.title));
    }
    private void render(){
        lightMode=prefs.getBoolean("lightMode",false);ink=lightMode?0xff202b3a:0xfff4efe5;muted=lightMode?0xff626976:0xffa6afbe;
        accent=lightMode?0xff866538:(prefs.getBoolean("saffron",true)?0xffdac295:0xffa6d5ca);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(42),dp(24),dp(42),dp(24));
        backdrop=new AuroraBackground();scroll.setBackground(backdrop);scroll.addView(page);setContentView(scroll);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=text("NAHATA  TV",18,ink);brand.setLetterSpacing(.16f);brand.setTypeface(null,Typeface.BOLD);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        Button jioShortcut=button("Jio ↗",this::openJio);jioShortcut.setTextSize(12);top.addView(jioShortcut,new LinearLayout.LayoutParams(dp(76),dp(36)));
        iconAction(top,"apps","Apps",()->{rearranging=false;allApps=!allApps;filter="";render();});
        iconAction(top,"wifi","Wi-Fi",()->launch(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        iconAction(top,"input","Inputs",this::inputs);
        iconAction(top,"remote","Phone remote",this::remoteInfo);
        iconAction(top,"paint","Personalise",this::personalise);
        iconAction(top,"settings","Settings",()->launch(new Intent(Settings.ACTION_SETTINGS)));
        iconAction(top,"info","About",this::about);
        clock=text("",13,muted);clock.setPadding(dp(18),0,0,0);top.addView(clock);page.addView(top);
        if(allApps){TextView title=text("Your collection",30,ink);title.setTypeface(Typeface.create("serif",Typeface.NORMAL));title.setPadding(0,dp(32),0,dp(18));page.addView(title);}
        else{page.addView(new View(this),new LinearLayout.LayoutParams(1,0,1));}
        search=new EditText(this);search.setSingleLine(true);search.setTextColor(ink);search.setHintTextColor(muted);search.setHint("Search all apps");search.setTextSize(14);style(search);if(allApps)page.addView(search,new LinearLayout.LayoutParams(-1,dp(44)));
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int st,int b,int c){filter=s.toString();drawApps();}public void afterTextChanged(android.text.Editable e){}});
        grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);grid.setGravity(Gravity.CENTER_HORIZONTAL);page.addView(grid);drawApps();
        TextView credit=text("Developed by Param Nahata",9,muted);credit.setGravity(Gravity.END);credit.setPadding(0,dp(18),0,0);page.addView(credit,new LinearLayout.LayoutParams(-1,dp(36)));
        handler.removeCallbacks(tick);handler.post(tick);if(allApps)top.getChildAt(2).requestFocus();startAmbient();
    }
    private void addAction(LinearLayout row,String name,Runnable action){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(0,0,dp(10),0);row.addView(button(name,action),p);}
    private Set<String> readSet(String name){return new HashSet<>(prefs.getStringSet(name,Collections.emptySet()));}
    private Set<String> homeApps(){return new HashSet<>(prefs.getStringSet("homeApps",new HashSet<>(Arrays.asList("com.netflix.ninja","com.amazon.amazonvideo.livingroom","com.google.android.youtube.tv","com.mediatek.wwtv.tvcenter"))));}
    private ArrayList<String> homeOrder(){
        ArrayList<String> order=new ArrayList<>();try{org.json.JSONArray saved=new org.json.JSONArray(prefs.getString("homeOrder","[]"));for(int i=0;i<saved.length();i++){String key=saved.optString(i);if(!key.isEmpty()&&!order.contains(key))order.add(key);}}catch(org.json.JSONException ignored){}
        for(String key:Arrays.asList("com.netflix.ninja","com.amazon.amazonvideo.livingroom","com.google.android.youtube.tv","com.mediatek.wwtv.tvcenter"))if(!order.contains(key))order.add(key);return order;
    }
    private void drawApps(){
        grid.removeAllViews();Set<String> home=homeApps();ArrayList<App> visible=new ArrayList<>();
        for(App a:apps)if((allApps||home.contains(a.key))&&a.title.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)))visible.add(a);
        if(!allApps){List<String> order=rearranging&&reorderDraft!=null?reorderDraft:homeOrder();visible.sort((a,b)->Integer.compare(order.contains(a.key)?order.indexOf(a.key):Integer.MAX_VALUE,order.contains(b.key)?order.indexOf(b.key):Integer.MAX_VALUE));}
        if(allApps)section("",visible);else homeDock(visible);
        if(visible.isEmpty())grid.addView(text(allApps?"No matching apps.":"",16,muted));
    }
    private void section(String title,List<App> list){
        if(!title.isEmpty()){TextView label=text(title,19,ink);label.setPadding(0,dp(22),0,dp(12));grid.addView(label);}
        HorizontalScrollView slider=new HorizontalScrollView(this);slider.setHorizontalScrollBarEnabled(false);slider.setClipChildren(false);slider.setPadding(dp(3),dp(4),dp(3),dp(4));
        LinearLayout row=new LinearLayout(this);row.setClipChildren(false);slider.addView(row);grid.addView(slider);
        for(int n=0;n<list.size();n++){
            App a=list.get(n);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);style(card);
            ImageView icon=new ImageView(this);icon.setImageDrawable(a.icon);card.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(42)));
            String titleApp=a.key.equals("com.amazon.amazonvideo.livingroom")?"Prime Video":a.title;
            TextView labelApp=text(titleApp,14,ink);labelApp.setGravity(Gravity.CENTER);labelApp.setMaxLines(2);card.addView(labelApp);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(allApps||prefs.getBoolean("compact",false)?136:174),dp(104));p.setMargins(0,0,dp(14),dp(6));row.addView(card,p);
            card.setOnClickListener(v->launch(a.intent));card.setOnLongClickListener(v->{appOptions(a);return true;});
        }
    }
    private boolean motion(){return Settings.Global.getFloat(getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0;}
    private String appTitle(App a){return a.key.equals("com.amazon.amazonvideo.livingroom")?"Prime Video":a.title;}
    private void homeDock(List<App> list){
        if(list.isEmpty())return;
        selectedIndex=Math.min(selectedIndex,list.size()-1);
        int slot=dp(prefs.getBoolean("compact",false)?116:136), padding=dp(14);
        int available=getResources().getDisplayMetrics().widthPixels-dp(84);
        HorizontalScrollView rail=new HorizontalScrollView(this);rail.setHorizontalScrollBarEnabled(false);rail.setClipToPadding(false);rail.setBackground(glassPanel());rail.setElevation(dp(6));
        int width=Math.min(available,slot*list.size()+padding*2);
        grid.addView(rail,new LinearLayout.LayoutParams(width,dp(138)));
        FrameLayout content=new FrameLayout(this);rail.addView(content,new HorizontalScrollView.LayoutParams(slot*list.size()+padding*2,-1));
        View selection=new View(this);selection.setBackground(bg(lightMode?0xaaffffff:0x20e7d7b2,lightMode?0x809b7d4b:0x80e3ceaa));
        FrameLayout.LayoutParams highlight=new FrameLayout.LayoutParams(slot-dp(4),dp(110));highlight.leftMargin=padding+dp(2);highlight.topMargin=dp(14);content.addView(selection,highlight);selection.setTranslationX((rearranging&&movedFrom>=0?movedFrom:selectedIndex)*slot);
        LinearLayout row=new LinearLayout(this);row.setPadding(padding,dp(14),padding,dp(14));content.addView(row,new FrameLayout.LayoutParams(-1,-1));ArrayList<View> cards=new ArrayList<>();
        for(int i=0;i<list.size();i++){
            App app=list.get(i);final int index=i;
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setFocusable(true);card.setClickable(true);card.setContentDescription(appTitle(app));card.setAlpha(i==selectedIndex?1f:.62f);row.addView(card,new LinearLayout.LayoutParams(slot,-1));cards.add(card);
            ImageView icon=new ImageView(this);icon.setImageDrawable(app.icon);card.addView(icon,new LinearLayout.LayoutParams(dp(43),dp(43)));
            TextView label=text(appTitle(app),12,ink);label.setGravity(Gravity.CENTER);label.setPadding(0,dp(10),0,0);card.addView(label);
            card.setOnClickListener(v->{if(rearranging)saveRearrange();else launch(app.intent);});card.setOnLongClickListener(v->{if(!rearranging)appOptions(app);return true;});
            if(rearranging&&movedFrom>=0&&(i==movedFrom||i==movedTo)){card.setTranslationX((i==movedTo?movedFrom-movedTo:movedTo-movedFrom)*slot);card.animate().translationX(0).setDuration(motion()?180:0).start();}
            card.setOnFocusChangeListener((v,focused)->{
                v.animate().cancel();v.animate().alpha(focused?1f:.62f).translationX(0f).scaleX(focused?1.04f:1f).scaleY(focused?1.04f:1f).setDuration(motion()?130:0).start();
                if(focused){selectedIndex=index;homeFocus=v;selection.animate().cancel();selection.animate().translationX(index*slot).setDuration(motion()?180:0).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();}
            });
            card.setOnKeyListener((v,key,event)->{if(event.getAction()==KeyEvent.ACTION_DOWN&&(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT)){int delta=key==KeyEvent.KEYCODE_DPAD_RIGHT?1:-1;if(rearranging){int next=index+delta;if(next>=0&&next<list.size()){Collections.swap(reorderDraft,index,next);movedFrom=index;movedTo=next;selectedIndex=next;drawApps();}}else{int next=(index+delta+cards.size())%cards.size();cards.get(next).requestFocus();}return true;}return false;});
        }
        homeFocus=cards.get(selectedIndex);homeFocus.requestFocus();
    }
    private GradientDrawable glassPanel(){
        GradientDrawable panel=new GradientDrawable(GradientDrawable.Orientation.TL_BR,lightMode?new int[]{0xd9ffffff,0x88ffffff}:new int[]{0x26e0e5f3,0x0fffffff});panel.setCornerRadius(dp(28));panel.setStroke(dp(1),lightMode?0xb3ffffff:0x45e8dfcc);return panel;
    }
    private void openJio(){
        android.media.tv.TvInputManager manager=(android.media.tv.TvInputManager)getSystemService(TV_INPUT_SERVICE);
        if(manager!=null)for(android.media.tv.TvInputInfo input:manager.getTvInputList()){
            String label=input.loadLabel(this).toString().replace(" ","").toLowerCase(Locale.ROOT);
            boolean verifiedHisense=input.getId().equals("com.hisense.tv.hitvinput/.hdmi.HdmiTvInputService/HW5");
            if(input.getType()==android.media.tv.TvInputInfo.TYPE_HDMI&&(label.contains("hdmi2")||verifiedHisense)){
                launch(new Intent(Intent.ACTION_VIEW,android.media.tv.TvContract.buildChannelUriForPassthroughInput(input.getId())));return;
            }
        }
        new PremiumDialog().setTitle("HDMI 2").setMessage("HDMI 2 could not be identified on this TV. Use your remote's Input button to select it.").setPositiveButton("OK",null).show();
    }
    private void inputs(){
        android.media.tv.TvInputManager manager=(android.media.tv.TvInputManager)getSystemService(TV_INPUT_SERVICE);
        ArrayList<android.media.tv.TvInputInfo> found=new ArrayList<>();ArrayList<String> labels=new ArrayList<>();
        if(manager!=null)for(android.media.tv.TvInputInfo i:manager.getTvInputList())if(i.isPassthroughInput()){found.add(i);labels.add(i.loadLabel(this).toString());}
        new PremiumDialog().setTitle("TV inputs").setItems(labels.toArray(new String[0]),(d,n)->launch(new Intent(Intent.ACTION_VIEW,android.media.tv.TvContract.buildChannelUriForPassthroughInput(found.get(n).getId())))).setNegativeButton("Close",null).show();
    }
    private void oldLauncher(){
        new PremiumDialog().setTitle("Switch to original launcher?").setMessage("Original launcher mode pauses Nahata's Home override. Open Nahata TV from Apps to return to your custom Home. No launcher is disabled or uninstalled.").setPositiveButton("Switch",(d,n)->{prefs.edit().putBoolean("originalMode",true).apply();launch(new Intent().setComponent(new ComponentName("com.google.android.tvlauncher","com.google.android.tvlauncher.MainActivity")));}).setNegativeButton("Cancel",null).show();
    }
    private void remoteInfo(){
        String address=getIntent().getStringExtra("remote_address");
        if(address!=null)prefs.edit().putString("remoteAddress",address).apply();
        new PremiumDialog().setTitle("Nahata Remote").setMessage("Open on your phone:\n\n"+prefs.getString("remoteAddress","Start the Nahata Remote bridge on your laptop to see its local address.")+"\n\nPhone, laptop and TV must share Wi-Fi. Enter the pairing code shown by the bridge. This version requires the laptop to stay awake.").setPositiveButton("OK",null).show();
    }
    private void iconAction(LinearLayout row,String kind,String label,Runnable action){
        IconView icon=new IconView(kind);style(icon);icon.setContentDescription(label);icon.setTooltipText(label);icon.setOnClickListener(v->action.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(36),dp(36));p.setMargins(dp(8),0,0,0);row.addView(icon,p);
    }
    private class IconView extends View {
        private final String kind;private final android.graphics.Paint p=new android.graphics.Paint(3);
        IconView(String value){super(MainActivity.this);kind=value;}
        @Override protected void onDraw(android.graphics.Canvas c){super.onDraw(c);c.save();c.translate(getWidth()/2f-dp(10),getHeight()/2f-dp(10));c.scale(dp(20)/24f,dp(20)/24f);p.setColor(ink);p.setStrokeWidth(1.6f);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeCap(android.graphics.Paint.Cap.ROUND);
            switch(kind){
                case "apps":for(int x=3;x<=14;x+=11)for(int y=3;y<=14;y+=11)c.drawRoundRect(x,y,x+7,y+7,1,1,p);break;
                case "wifi":c.drawArc(-4,6,28,38,225,90,false,p);c.drawArc(1,11,23,33,225,90,false,p);c.drawArc(6,16,18,28,225,90,false,p);p.setStyle(android.graphics.Paint.Style.FILL);c.drawCircle(12,22,1.1f,p);break;
                case "input":c.drawRoundRect(3,4,21,18,2,2,p);c.drawLine(9,22,15,22,p);c.drawLine(12,18,12,22,p);c.drawLine(8,11,16,11,p);c.drawLine(13,8,16,11,p);c.drawLine(13,14,16,11,p);break;
                case "remote":c.drawRoundRect(6,1,18,23,4,4,p);c.drawCircle(12,9,3,p);c.drawLine(10,17,14,17,p);break;
                case "paint":c.drawLine(5,19,18,6,p);c.drawLine(15,3,21,9,p);c.drawCircle(5,19,3,p);break;
                case "settings":c.drawCircle(12,12,7,p);c.drawCircle(12,12,2.5f,p);for(int a=0;a<8;a++){double t=a*Math.PI/4;c.drawLine(12+(float)Math.cos(t)*8,12+(float)Math.sin(t)*8,12+(float)Math.cos(t)*10,12+(float)Math.sin(t)*10,p);}break;
                default:c.drawCircle(12,12,9,p);c.drawLine(12,11,12,17,p);c.drawCircle(12,7,0.6f,p);
            }c.restore();
        }
    }
    /** Native dialogs with the same pearl/navy glass palette as Home. */
    private class PremiumDialog {
        private String title="",message,positive,negative;private String[] items;private View custom;
        private DialogInterface.OnClickListener chosen,yes,no;
        PremiumDialog setTitle(String value){title=value;return this;}
        PremiumDialog setMessage(String value){message=value;return this;}
        PremiumDialog setItems(String[] values,DialogInterface.OnClickListener action){items=values;chosen=action;return this;}
        PremiumDialog setView(View value){custom=value;return this;}
        PremiumDialog setPositiveButton(String label,DialogInterface.OnClickListener action){positive=label;yes=action;return this;}
        PremiumDialog setNegativeButton(String label,DialogInterface.OnClickListener action){negative=label;no=action;return this;}
        Dialog show(){
            Dialog dialog=new Dialog(MainActivity.this);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            LinearLayout body=new LinearLayout(MainActivity.this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(28),dp(24),dp(28),dp(24));
            GradientDrawable surface=new GradientDrawable(GradientDrawable.Orientation.TL_BR,lightMode?new int[]{0xfffaf7f0,0xffe8e9ed}:new int[]{0xff202b40,0xff111d2d});surface.setCornerRadius(dp(25));surface.setStroke(dp(1),lightMode?0x90ffffff:0x60d6c19b);body.setBackground(surface);
            TextView heading=text(title,25,ink);heading.setTypeface(Typeface.create("serif",Typeface.NORMAL));heading.setPadding(0,0,0,dp(18));body.addView(heading);
            if(message!=null){TextView copy=text(message,15,muted);copy.setLineSpacing(dp(4),1);copy.setPadding(0,0,0,dp(18));body.addView(copy);}
            if(custom!=null){if(custom instanceof EditText){EditText edit=(EditText)custom;style(edit);edit.setTextColor(ink);edit.setTextSize(17);}body.addView(custom,new LinearLayout.LayoutParams(-1,dp(54)));}
            if(items!=null){ScrollView scroll=new ScrollView(MainActivity.this);scroll.setVerticalScrollBarEnabled(false);LinearLayout rows=new LinearLayout(MainActivity.this);rows.setOrientation(LinearLayout.VERTICAL);scroll.addView(rows);body.addView(scroll,new LinearLayout.LayoutParams(-1,Math.min(dp(260),dp(50)*items.length)));
                for(int n=0;n<items.length;n++){final int index=n;Button entry=button(items[n],()->{dialog.dismiss();if(chosen!=null)chosen.onClick(dialog,index);});entry.setTextSize(14);entry.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);entry.setElevation(0);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(46));rp.bottomMargin=dp(4);rows.addView(entry,rp);}
            }
            LinearLayout actions=new LinearLayout(MainActivity.this);actions.setGravity(Gravity.END);actions.setPadding(0,dp(14),0,0);
            if(negative!=null){Button cancel=button(negative,()->{dialog.dismiss();if(no!=null)no.onClick(dialog,DialogInterface.BUTTON_NEGATIVE);});cancel.setTextSize(13);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(44));p.rightMargin=dp(10);actions.addView(cancel,p);}
            if(positive!=null){Button confirm=button(positive,()->{dialog.dismiss();if(yes!=null)yes.onClick(dialog,DialogInterface.BUTTON_POSITIVE);});confirm.setTextColor(accent);confirm.setTextSize(13);actions.addView(confirm,new LinearLayout.LayoutParams(-2,dp(44)));}
            if(actions.getChildCount()>0)body.addView(actions);
            dialog.setContentView(body);Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawableResource(android.R.color.transparent);window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attrs=window.getAttributes();attrs.dimAmount=.55f;window.setAttributes(attrs);}
            dialog.show();if(window!=null)window.setLayout(Math.min(dp(500),getResources().getDisplayMetrics().widthPixels-dp(84)),WindowManager.LayoutParams.WRAP_CONTENT);return dialog;
        }
    }
    private void appearance(){new PremiumDialog().setTitle("Appearance").setItems(new String[]{"Midnight · Dark","Pearl · Light"},(d,n)->{prefs.edit().putBoolean("lightMode",n==1).apply();render();}).setNegativeButton("Close",null).show();}
    private String wallpaper(){return prefs.getString("wallpaper",prefs.getBoolean("scenic",true)?"alpine":"aurora");}
    private void backgrounds(){new PremiumDialog().setTitle("Background").setItems(new String[]{"Alpine · Day / Night","Sunset · Golden hour","Aurora · Gradient"},(d,n)->{prefs.edit().putString("wallpaper",new String[]{"alpine","sunset","aurora"}[n]).apply();render();}).setNegativeButton("Close",null).show();}
    private class AuroraBackground extends android.graphics.drawable.Drawable {
        private final android.graphics.Paint paint=new android.graphics.Paint(3);
        @Override public void draw(android.graphics.Canvas c){
            android.graphics.Rect r=getBounds();float w=r.width(),h=r.height();
            if(!wallpaper().equals("aurora")){
                int resource=wallpaper().equals("sunset")?R.drawable.alpine_lake:(lightMode?R.drawable.alpine_day:R.drawable.alpine_night);
                if(scenic==null||scenicResource!=resource){scenic=android.graphics.BitmapFactory.decodeResource(getResources(),resource);scenicResource=resource;}
                if(scenic!=null){float scale=Math.max(w/scenic.getWidth(),h/scenic.getHeight())*(1.04f+.012f*ambientPhase);float bw=scenic.getWidth()*scale,bh=scenic.getHeight()*scale;float drift=w*.008f*(ambientPhase-.5f);paint.setShader(null);paint.setColor(Color.WHITE);c.drawBitmap(scenic,null,new android.graphics.RectF((w-bw)/2+drift,(h-bh)/2,(w+bw)/2+drift,(h+bh)/2),paint);
                    if(lightMode){paint.setShader(new android.graphics.LinearGradient(0,0,0,h,new int[]{0xaafff8ed,0x10fff8ed,0x88fff8ed},null,android.graphics.Shader.TileMode.CLAMP));c.drawRect(r,paint);}else{paint.setShader(new android.graphics.LinearGradient(0,0,0,h,new int[]{0xb308101c,0x1008101c,0x9008101c},null,android.graphics.Shader.TileMode.CLAMP));c.drawRect(r,paint);}return;
                }
            }
            paint.setShader(new android.graphics.LinearGradient(0,0,w,h,lightMode?new int[]{0xfff5f0e6,0xffe7e8ee,0xffe5ddcd}:new int[]{0xff080f1d,0xff172237,0xff101b2d},null,android.graphics.Shader.TileMode.CLAMP));c.drawRect(r,paint);
            glow(c,w*(.82f+.04f*ambientPhase),h*.18f,w*.66f,lightMode?0xfff9ead2:0xff39324e);glow(c,w*(.22f-.04f*ambientPhase),h*.92f,w*.64f,lightMode?0xffcbd9e0:0xff25444e);glow(c,w*.52f,h*.64f,w*.44f,lightMode?0x70ffffff:0x30b29968);
        }
        private void glow(android.graphics.Canvas c,float x,float y,float radius,int color){paint.setShader(new android.graphics.RadialGradient(x,y,radius,new int[]{color,Color.TRANSPARENT},null,android.graphics.Shader.TileMode.CLAMP));c.drawCircle(x,y,radius,paint);}
        @Override public void setAlpha(int a){} @Override public void setColorFilter(android.graphics.ColorFilter f){} @Override public int getOpacity(){return android.graphics.PixelFormat.OPAQUE;}
    }
    private void appOptions(App a){
        Set<String> home=homeApps();new PremiumDialog().setTitle(a.title).setItems(new String[]{home.contains(a.key)?"Hide on Home screen":"Show on Home screen","Rearrange","Open app","App information"},(d,n)->{
            if(n==0){if(!home.add(a.key))home.remove(a.key);prefs.edit().putStringSet("homeApps",home).apply();drawApps();}
            if(n==1)startRearrange(a);
            if(n==2)launch(a.intent);
            if(n==3)launch(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+a.key)));}).show();
    }
    private void startRearrange(App app){
        Set<String> home=homeApps();if(!home.contains(app.key)){new PremiumDialog().setTitle("Rearrange Home").setMessage("Show this app on Home first.").setPositiveButton("OK",null).show();return;}
        reorderDraft=homeOrder();for(App a:apps)if(home.contains(a.key)&&!reorderDraft.contains(a.key))reorderDraft.add(a.key);
        Set<String> installed=new HashSet<>();for(App a:apps)installed.add(a.key);reorderDraft.removeIf(key->!home.contains(key)||!installed.contains(key));
        selectedIndex=Math.max(0,reorderDraft.indexOf(app.key));allApps=false;filter="";rearranging=true;movedFrom=-1;render();Toast.makeText(this,"Left / Right to move · OK to save · Back to cancel",Toast.LENGTH_LONG).show();
    }
    private void saveRearrange(){
        if(reorderDraft==null)return;ArrayList<String> order=new ArrayList<>(reorderDraft);for(String key:homeOrder())if(!order.contains(key))order.add(key);
        prefs.edit().putString("homeOrder",new org.json.JSONArray(order).toString()).apply();rearranging=false;reorderDraft=null;movedFrom=-1;drawApps();
    }
    private void personalise(){new PremiumDialog().setTitle("Make it yours").setItems(new String[]{"Appearance · Dark / Light","Background","Accent · Champagne / Jade","Dock · Spacious / Compact","Reset Home apps","Switch to original launcher","System accessibility settings","Ambient motion · On / Off"},(d,n)->{
        if(n==0)appearance();
        if(n==1)backgrounds();
        if(n==2){prefs.edit().putBoolean("saffron",!prefs.getBoolean("saffron",true)).apply();render();}
        if(n==3){prefs.edit().putBoolean("compact",!prefs.getBoolean("compact",false)).apply();render();}
        if(n==4){prefs.edit().remove("homeApps").remove("homeOrder").apply();rearranging=false;drawApps();}
        if(n==6)launch(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        if(n==5)oldLauncher();
        if(n==7){prefs.edit().putBoolean("ambientMotion",!prefs.getBoolean("ambientMotion",true)).apply();startAmbient();}
    }).show();}
    private void about(){new PremiumDialog().setTitle("नाहटा • Nahata TV").setMessage("Developed by Param Nahata\n\nYour home. Your screen.\nVersion 0.5.0 • MIT License\n\nOriginal Android TV launcher. No ads, accounts, analytics or network permission.\n\nHold OK on an app to show or hide it on Home. All apps remain in Apps. Jio opens HDMI 2 on supported TVs.").setPositiveButton("paramnahata.com",(d,n)->launch(new Intent(Intent.ACTION_VIEW,Uri.parse("https://paramnahata.com")))).setNegativeButton("Close",null).show();}
}
