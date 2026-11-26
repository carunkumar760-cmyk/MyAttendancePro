package com.example.myattendancepro;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import android.content.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private LinearLayout root, content;
    private TextView status, totals;
    private EditText salaryInput, daysInput;
    private String mode = "Personal";
    private android.content.SharedPreferences prefs;
    private final int bg = Color.rgb(12,18,32), panel = Color.rgb(25,35,55), blue = Color.rgb(55,135,255);

    public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("attendance", MODE_PRIVATE);
        showHome();
    }
    private String today() { return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()); }
    private String now() { return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()); }
    private String key(String m, String d, String part) { return m+"_"+d+"_"+part; }
    private void base(String title) {
        root = new LinearLayout(this); root.setOrientation(1); root.setPadding(20,22,20,18); root.setBackgroundColor(bg);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        content = new LinearLayout(this); content.setOrientation(1); content.setPadding(0,0,0,24);
        TextView h = new TextView(this); h.setText(title); h.setTextColor(Color.WHITE); h.setTextSize(25); h.setTypeface(null,1);
        content.addView(h); scroll.addView(content); root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    private TextView text(String s, int size) {
        TextView t = new TextView(this); t.setText(s); t.setTextColor(Color.WHITE); t.setTextSize(size); t.setPadding(0,10,0,10); return t;
    }
    private void card(String s) {
        TextView t=text(s,16); t.setBackgroundColor(panel); t.setPadding(16,16,16,16);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,10,0,4); content.addView(t,p);
    }
    private Button btn(String s, Runnable r) {
        Button b=new Button(this); b.setText(s); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue));
        content.addView(b,new LinearLayout.LayoutParams(-1,-2)); b.setOnClickListener(v->r.run()); return b;
    }
    private void showHome() {
        base("MY ATTENDANCE PRO");
        content.addView(text("Your attendance, in one place",14));
        card("Selected: "+mode+"\nToday: "+today()+"\nIn: "+value(key(mode,today(),"in"))+"\nOut: "+value(key(mode,today(),"out")));
        btn("Personal Attendance",()->{mode="Personal";showHome();});
        btn("Company Attendance",()->{mode="Company";showHome();});
        btn("Punch IN  •  "+now(),()->{prefs.edit().putString(key(mode,today(),"in"),now()).apply();showHome();});
        btn("Punch OUT  •  "+now(),()->{prefs.edit().putString(key(mode,today(),"out"),now()).apply();showHome();});
        btn("Attendance History",()->showHistory());
        btn("Monthly Summary",()->showSummary());
        btn("Salary Calculator",()->showSalary());
        btn("About / Data",()->showAbout());
        card("Note: Company attendance is recorded manually in this first version. Hikvision/company-system syncing needs official access from your employer.");
    }
    private String value(String k) { return prefs.getString(k,"Not recorded"); }
    private void showHistory() {
        base(mode+" HISTORY");
        btn("Back",()->showHome());
        for(int i=0;i<31;i++){
            Calendar c=Calendar.getInstance(); c.add(Calendar.DAY_OF_YEAR,-i);
            String d=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.getTime());
            String a=prefs.getString(key(mode,d,"in"),"-"), z=prefs.getString(key(mode,d,"out"),"-");
            if(!a.equals("-")||!z.equals("-")) card(d+"    IN: "+a+"    OUT: "+z);
        }
        card("Only dates with a recorded punch are shown.");
    }
    private void showSummary() {
        base("MONTHLY SUMMARY");
        btn("Back",()->showHome());
        Calendar c=Calendar.getInstance(); int month=c.get(Calendar.MONTH), year=c.get(Calendar.YEAR);
        int count=0; long minutes=0;
        int max=c.getActualMaximum(Calendar.DAY_OF_MONTH);
        for(int d=1;d<=max;d++){
            Calendar x=new GregorianCalendar(year,month,d);
            String ds=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(x.getTime());
            String a=prefs.getString(key(mode,ds,"in"),null), z=prefs.getString(key(mode,ds,"out"),null);
            if(a!=null) count++;
            if(a!=null && z!=null) try {
                SimpleDateFormat f=new SimpleDateFormat("hh:mm a",Locale.getDefault());
                Date start=f.parse(a), end=f.parse(z);
                long diff=end.getTime()-start.getTime(); if(diff<0) diff+=24*60*60*1000;
                minutes+=TimeUnit.MILLISECONDS.toMinutes(diff);
            } catch(Exception ignored){}
        }
        card("Month: "+new SimpleDateFormat("MMMM yyyy",Locale.getDefault()).format(new Date())+
             "\n"+mode+" days punched: "+count+"\nTotal recorded hours: "+(minutes/60)+"h "+(minutes%60)+"m");
        card("Overtime is not auto-classified yet. Compare total hours with your company shift rules.");
    }
    private void showSalary() {
        base("SALARY ESTIMATE");
        btn("Back",()->showHome());
        salaryInput=new EditText(this); salaryInput.setSingleLine(); salaryInput.setHint("Monthly salary (₹)"); salaryInput.setInputType(2); content.addView(salaryInput);
        daysInput=new EditText(this); daysInput.setSingleLine(); daysInput.setHint("Paid working days in month (e.g. 26)"); daysInput.setInputType(2); content.addView(daysInput);
        btn("Calculate per-day salary",()->{
            try { double s=Double.parseDouble(salaryInput.getText().toString()); double d=Double.parseDouble(daysInput.getText().toString());
                if(d<=0) throw new Exception();
                card("Estimated per-day salary: ₹"+String.format(Locale.getDefault(),"%.2f",s/d));
            } catch(Exception e){ Toast.makeText(this,"Enter valid salary and working days",Toast.LENGTH_SHORT).show(); }
        });
        card("This is an estimate only. Actual salary depends on company policy, deductions, overtime and paid leave.");
    }
    private void showAbout() {
        base("ABOUT");
        btn("Back",()->showHome());
        card("My Attendance Pro v1.0\nWorks offline and stores punch records on this phone.\nNo company login or password is collected.\nBack up your phone data to avoid losing records.");
    }
}
