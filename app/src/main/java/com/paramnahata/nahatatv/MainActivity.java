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
    private final Runnable tick = new Runnable() { public void run() {
        if (clock != null) clock.setText(new SimpleDateFormat("EEE, dd MMM   •   HH:mm", Locale.getDefault()).format(new Date()));
        handler.postDelayed(this, 15000);
    }};
    static class App {
        String title, key; android.graphics.drawable.Drawable icon; Intent intent;
        App(String t, String k, android.graphics.drawable.Drawable d, Intent i) {title=t;key=k;icon=d;intent=i;}
    }
    @Override public void onCreate(Bundle state) {super.onCreate(state); prefs=getSharedPreferences("home",MODE_PRIVATE); loadApps(); render();if(state==null) entrance();}
    @Override public void onResume(){super.onResume();loadApps();if(grid!=null)drawApps();handler.removeCallbacks(tick);handler.post(tick);}
    @Override public void onPause(){handler.removeCallbacks(tick);super.onPause();}
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
    private void style(View v){v.setFocusable(true);v.setPadding(dp(16),dp(12),dp(16),dp(12));v.setBackground(bg(0x19ffffff,0x30ffffff));v.setOnFocusChangeListener((w,f)->{w.setBackground(bg(f?0x38ffffff:0x19ffffff,f?accent:0x30ffffff));w.setScaleX(f?1.025f:1);w.setScaleY(f?1.025f:1);});}
    private Button button(String title,Runnable action){Button b=new Button(this);b.setText(title);b.setTextColor(ink);b.setTextSize(15);b.setAllCaps(false);style(b);b.setOnClickListener(v->action.run());return b;}
    private void launch(Intent i){try{startActivity(i);}catch(Exception e){new AlertDialog.Builder(this).setTitle("Unavailable on this TV").setMessage("This firmware does not expose that screen or app. Use the physical remote's Input or Settings button.").setPositiveButton("OK",null).show();}}
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
        accent=prefs.getBoolean("saffron",true)?0xffedbc79:0xff8bd4bd;
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(42),dp(24),dp(42),dp(24));
        scroll.setBackground(new AuroraBackground());scroll.addView(page);setContentView(scroll);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=text("NAHATA  TV",18,ink);brand.setLetterSpacing(.16f);brand.setTypeface(null,Typeface.BOLD);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        clock=text("",16,muted);top.addView(clock);page.addView(top);
        LinearLayout hero=new LinearLayout(this);hero.setGravity(Gravity.CENTER_VERTICAL);hero.setPadding(0,dp(26),0,dp(20));
        LinearLayout welcome=new LinearLayout(this);welcome.setOrientation(LinearLayout.VERTICAL);
        TextView eyebrow=text("A LITTLE LESS NOISE. A LOT MORE YOU.",11,accent);eyebrow.setLetterSpacing(.12f);welcome.addView(eyebrow);
        TextView greeting=text(prefs.getString("greeting","Your home. Your screen."),32,ink);greeting.setPadding(0,dp(8),0,dp(8));welcome.addView(greeting);
        welcome.addView(text("Settle in. Everything you love is right here.",14,muted));hero.addView(welcome,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout jio=new LinearLayout(this);jio.setOrientation(LinearLayout.VERTICAL);style(jio);jio.setGravity(Gravity.CENTER_VERTICAL);
        TextView live=text("YOUR SET-TOP BOX",10,accent);live.setLetterSpacing(.12f);jio.addView(live);jio.addView(text("Jio  ↗",27,ink));jio.addView(text("HDMI 2  •  Watch TV",13,muted));jio.setOnClickListener(v->openJio());hero.addView(jio,new LinearLayout.LayoutParams(dp(228),dp(114)));page.addView(hero);
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER_VERTICAL);actions.setPadding(0,0,0,dp(8));
        addAction(actions,"Settings",()->launch(new Intent(Settings.ACTION_SETTINGS)));
        addAction(actions,"Personalise",this::personalise);addAction(actions,"About",this::about);
        search=new EditText(this);search.setSingleLine(true);search.setTextColor(ink);search.setHintTextColor(muted);search.setHint("Search apps");search.setTextSize(14);style(search);actions.addView(search,new LinearLayout.LayoutParams(dp(218),dp(46)));page.addView(actions);
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int st,int b,int c){filter=s.toString();drawApps();}public void afterTextChanged(android.text.Editable e){}});
        grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);page.addView(grid);drawApps();
        TextView credit=text("Developed by Param Nahata   /   paramnahata.com",10,muted);credit.setPadding(0,dp(18),0,0);page.addView(credit);
    }
    private void addAction(LinearLayout row,String name,Runnable action){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(0,0,dp(10),0);row.addView(button(name,action),p);}
    private Set<String> readSet(String name){return new HashSet<>(prefs.getStringSet(name,Collections.emptySet()));}
    private void drawApps(){
        grid.removeAllViews();Set<String> fav=readSet("favourites"),hidden=readSet("hidden");
        ArrayList<App> favourites=new ArrayList<>(), visible=new ArrayList<>();
        for(App a:apps)if(!hidden.contains(a.key)&&a.title.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))){if(fav.contains(a.key))favourites.add(a);visible.add(a);}
        if(!favourites.isEmpty())section("Favourites",favourites);
        section("All apps",visible);
        if(visible.isEmpty())grid.addView(text("No matching apps. Hidden apps can be restored in Personalise.",16,muted));
    }
    private void section(String title,List<App> list){
        TextView label=text(title,19,ink);label.setPadding(0,dp(22),0,dp(12));grid.addView(label);
        HorizontalScrollView slider=new HorizontalScrollView(this);slider.setHorizontalScrollBarEnabled(false);slider.setClipChildren(false);slider.setPadding(dp(3),dp(4),dp(3),dp(4));
        LinearLayout row=new LinearLayout(this);row.setClipChildren(false);slider.addView(row);grid.addView(slider);
        for(int n=0;n<list.size();n++){
            App a=list.get(n);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);style(card);
            ImageView icon=new ImageView(this);icon.setImageDrawable(a.icon);card.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(42)));
            TextView labelApp=text(a.title,14,ink);labelApp.setGravity(Gravity.CENTER);labelApp.setMaxLines(2);card.addView(labelApp);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(prefs.getBoolean("compact",false)?122:154),dp(104));p.setMargins(0,0,dp(12),dp(6));row.addView(card,p);
            card.setOnClickListener(v->launch(a.intent));card.setOnLongClickListener(v->{appOptions(a);return true;});
        }
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
        new AlertDialog.Builder(this).setTitle("HDMI 2").setMessage("HDMI 2 could not be identified on this TV. Use your remote's Input button to select it.").setPositiveButton("OK",null).show();
    }
    private class AuroraBackground extends android.graphics.drawable.Drawable {
        private final android.graphics.Paint paint=new android.graphics.Paint(3);
        @Override public void draw(android.graphics.Canvas c){
            android.graphics.Rect r=getBounds();float w=r.width(),h=r.height();
            paint.setShader(new android.graphics.LinearGradient(0,0,w,h,new int[]{0xff10142b,0xff172940,0xff102e35},null,android.graphics.Shader.TileMode.CLAMP));c.drawRect(r,paint);
            glow(c,w*.82f,h*.16f,w*.62f,0xff52458e);glow(c,w*.24f,h*.86f,w*.62f,0xff126a75);
        }
        private void glow(android.graphics.Canvas c,float x,float y,float radius,int color){paint.setShader(new android.graphics.RadialGradient(x,y,radius,new int[]{color,Color.TRANSPARENT},null,android.graphics.Shader.TileMode.CLAMP));c.drawCircle(x,y,radius,paint);}
        @Override public void setAlpha(int a){} @Override public void setColorFilter(android.graphics.ColorFilter f){} @Override public int getOpacity(){return android.graphics.PixelFormat.OPAQUE;}
    }
    private void appOptions(App a){
        Set<String> fav=readSet("favourites");new AlertDialog.Builder(this).setTitle(a.title).setItems(new String[]{fav.contains(a.key)?"Remove favourite":"Add favourite","Hide from Nahata TV","App information"},(d,n)->{
            if(n==0){if(!fav.add(a.key))fav.remove(a.key);prefs.edit().putStringSet("favourites",fav).apply();}
            if(n==1){Set<String> h=readSet("hidden");h.add(a.key);prefs.edit().putStringSet("hidden",h).apply();}
            if(n==2)launch(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+a.key)));drawApps();}).show();
    }
    private void personalise(){new AlertDialog.Builder(this).setTitle("Make it your Nahata TV").setItems(new String[]{"Switch saffron / mint accent","Switch spacious / compact slider","Edit welcome message","Restore all hidden apps","Choose default home app"},(d,n)->{
        if(n==0){prefs.edit().putBoolean("saffron",!prefs.getBoolean("saffron",true)).apply();render();}
        if(n==1){prefs.edit().putBoolean("compact",!prefs.getBoolean("compact",false)).apply();render();}
        if(n==2){EditText input=new EditText(this);input.setSingleLine(true);input.setText(prefs.getString("greeting","Your home. Your screen."));new AlertDialog.Builder(this).setTitle("Welcome message").setView(input).setPositiveButton("Save",(x,y)->{String s=input.getText().toString().trim();prefs.edit().putString("greeting",s.isEmpty()?"Your home. Your screen.":s).apply();render();}).setNegativeButton("Cancel",null).show();}
        if(n==3){prefs.edit().remove("hidden").apply();drawApps();}
        if(n==4)launch(new Intent(Settings.ACTION_HOME_SETTINGS));
    }).show();}
    private void about(){new AlertDialog.Builder(this).setTitle("नाहटा • Nahata TV").setMessage("Developed by Param Nahata\n\nYour home. Your screen.\nVersion 0.2.0 • MIT License\n\nOriginal Android TV launcher. No ads, accounts, analytics or network permission.\n\nLong-press any app to favourite, hide or inspect it. Jio opens HDMI 2 on supported TVs.").setPositiveButton("paramnahata.com",(d,n)->launch(new Intent(Intent.ACTION_VIEW,Uri.parse("https://paramnahata.com")))).setNegativeButton("Close",null).show();}
}
