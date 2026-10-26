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
    private void style(View v){v.setFocusable(true);v.setPadding(dp(16),dp(12),dp(16),dp(12));v.setBackground(bg(0xff213630,0));v.setOnFocusChangeListener((w,f)->{w.setBackground(bg(f?0xff36554a:0xff213630,f?accent:0));w.setScaleX(f?1.025f:1);w.setScaleY(f?1.025f:1);});}
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
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(42),dp(28),dp(42),dp(28));
        page.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff0c211d,0xff193c32,0xff122923}));scroll.addView(page);setContentView(scroll);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=text("नाहटा  /  NAHATA TV",23,accent);brand.setTypeface(null,Typeface.BOLD);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        clock=text("",16,muted);top.addView(clock);page.addView(top);
        TextView greeting=text(prefs.getString("greeting","Your home. Your screen."),36,ink);greeting.setPadding(0,dp(28),0,dp(4));page.addView(greeting);
        page.addView(text("A quieter place for your favourite apps.",16,muted));
        LinearLayout actions=new LinearLayout(this);actions.setPadding(0,dp(22),0,dp(18));
        addAction(actions,"Inputs / Jio",()->launch(new Intent("android.settings.TV_INPUT_SETTINGS")));
        addAction(actions,"TV Settings",()->launch(new Intent(Settings.ACTION_SETTINGS)));
        addAction(actions,"Personalise",this::personalise);
        addAction(actions,"About",this::about);page.addView(actions);
        search=new EditText(this);search.setSingleLine(true);search.setTextColor(ink);search.setHintTextColor(muted);search.setHint("Search installed apps");search.setTextSize(16);style(search);page.addView(search,new LinearLayout.LayoutParams(-1,dp(52)));
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int st,int b,int c){filter=s.toString();drawApps();}public void afterTextChanged(android.text.Editable e){}});
        grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);page.addView(grid);drawApps();
        TextView credit=text("Developed by Param Nahata  •  paramnahata.com",12,muted);credit.setPadding(0,dp(28),0,0);page.addView(credit);
    }
    private void addAction(LinearLayout row,String name,Runnable action){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(54),1);p.setMargins(0,0,dp(10),0);row.addView(button(name,action),p);}
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
        LinearLayout row=null;int columns=prefs.getBoolean("compact",false)?6:4;
        for(int n=0;n<list.size();n++){
            if(n%columns==0){row=new LinearLayout(this);grid.addView(row);}
            App a=list.get(n);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);style(card);
            ImageView icon=new ImageView(this);icon.setImageDrawable(a.icon);card.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(42)));
            TextView labelApp=text(a.title,14,ink);labelApp.setGravity(Gravity.CENTER);labelApp.setMaxLines(2);card.addView(labelApp);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(112),1);p.setMargins(0,0,dp(12),dp(12));row.addView(card,p);
            card.setOnClickListener(v->launch(a.intent));card.setOnLongClickListener(v->{appOptions(a);return true;});
        }
        if(row!=null){int missing=(columns-list.size()%columns)%columns;for(int i=0;i<missing;i++)row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));}
    }
    private void appOptions(App a){
        Set<String> fav=readSet("favourites");new AlertDialog.Builder(this).setTitle(a.title).setItems(new String[]{fav.contains(a.key)?"Remove favourite":"Add favourite","Hide from Nahata TV","App information"},(d,n)->{
            if(n==0){if(!fav.add(a.key))fav.remove(a.key);prefs.edit().putStringSet("favourites",fav).apply();}
            if(n==1){Set<String> h=readSet("hidden");h.add(a.key);prefs.edit().putStringSet("hidden",h).apply();}
            if(n==2)launch(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+a.key)));drawApps();}).show();
    }
    private void personalise(){new AlertDialog.Builder(this).setTitle("Make it your Nahata TV").setItems(new String[]{"Switch saffron / mint accent","Switch spacious / compact grid","Edit welcome message","Restore all hidden apps","Choose default home app"},(d,n)->{
        if(n==0){prefs.edit().putBoolean("saffron",!prefs.getBoolean("saffron",true)).apply();render();}
        if(n==1){prefs.edit().putBoolean("compact",!prefs.getBoolean("compact",false)).apply();render();}
        if(n==2){EditText input=new EditText(this);input.setSingleLine(true);input.setText(prefs.getString("greeting","Your home. Your screen."));new AlertDialog.Builder(this).setTitle("Welcome message").setView(input).setPositiveButton("Save",(x,y)->{String s=input.getText().toString().trim();prefs.edit().putString("greeting",s.isEmpty()?"Your home. Your screen.":s).apply();render();}).setNegativeButton("Cancel",null).show();}
        if(n==3){prefs.edit().remove("hidden").apply();drawApps();}
        if(n==4)launch(new Intent(Settings.ACTION_HOME_SETTINGS));
    }).show();}
    private void about(){new AlertDialog.Builder(this).setTitle("नाहटा • Nahata TV").setMessage("Developed by Param Nahata\n\nYour home. Your screen.\nVersion 0.1.0 • MIT License\n\nOriginal Android TV launcher. No ads, accounts, analytics or network permission.\n\nLong-press any app to favourite, hide or inspect it. Inputs opens the TV's input settings; available actions depend on the firmware.").setPositiveButton("paramnahata.com",(d,n)->launch(new Intent(Intent.ACTION_VIEW,Uri.parse("https://paramnahata.com")))).setNegativeButton("Close",null).show();}
}
