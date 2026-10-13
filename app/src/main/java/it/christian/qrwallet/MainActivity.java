package it.christian.qrwallet;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.zip.*;

public class MainActivity extends Activity {
    private static final int MIN_ITEMS=8, PICK_THUMB=101, PICK_CODE=102, EXPORT_ZIP=201, IMPORT_ZIP=202;
    private SharedPreferences prefs;
    private LinearLayout root;
    private EditText labelField;
    private int selected=0;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("items",MODE_PRIVATE);
        if(!prefs.contains("count")) prefs.edit().putInt("count",MIN_ITEMS).apply();
        showHome();
    }

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private int count(){return Math.max(MIN_ITEMS,prefs.getInt("count",MIN_ITEMS));}
    private String value(String key,int i){return prefs.getString(key+i,"");}
    private TextView text(String s,int size,int color,int gravity){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setGravity(gravity);v.setPadding(dp(10),dp(8),dp(10),dp(8));return v;}
    private GradientDrawable bg(int color,int stroke,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));if(stroke!=0)g.setStroke(dp(1),stroke);return g;}

    private void shell(String title){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(248,250,252));
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setBackgroundColor(Color.rgb(29,78,216));
        bar.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(dp(8),i.getSystemWindowInsetTop()+dp(16),dp(10),dp(10));return i;});
        Button menu=new Button(this);menu.setText("☰");menu.setTextSize(24);menu.setTextColor(Color.WHITE);menu.setBackgroundColor(Color.TRANSPARENT);menu.setOnClickListener(v->showMenu(menu));
        bar.addView(menu,new LinearLayout.LayoutParams(dp(58),dp(58)));
        TextView h=text(title,21,Color.WHITE,Gravity.CENTER_VERTICAL);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);bar.addView(h,new LinearLayout.LayoutParams(0,dp(58),1));
        root.addView(bar);setContentView(root);
    }

    private void showMenu(View anchor){
        PopupMenu m=new PopupMenu(this,anchor);
        for(String s:new String[]{"HOME","GESTISCI CONTENUTI","ESPORTA","IMPORTA","INFORMAZIONI"})m.getMenu().add(s);
        m.setOnMenuItemClickListener(x->{saveLabel();String s=x.getTitle().toString();if(s.equals("HOME"))showHome();else if(s.startsWith("GESTISCI"))showEditor();else if(s.equals("ESPORTA"))startExport();else if(s.equals("IMPORTA"))startImport();else showInfo();return true;});m.show();
    }

    private void showHome(){
        labelField=null;shell("HOME");ScrollView scroll=new ScrollView(this);LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        GridLayout grid=new GridLayout(this);grid.setColumnCount(2);grid.setPadding(dp(8),dp(8),dp(8),dp(6));
        for(int i=0;i<count();i++){final int index=i;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(6),dp(5),dp(6),dp(5));card.setBackground(bg(Color.WHITE,Color.rgb(226,232,240),14));
            String thumb=value("t",i);if(!thumb.isEmpty()&&new File(thumb).exists()){ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setImageURI(Uri.fromFile(new File(thumb)));card.addView(image,new LinearLayout.LayoutParams(-1,dp(82)));}
            String name=value("n",i);TextView title=text(name.isEmpty()?"Elemento "+(i+1):name,16,Color.rgb(17,24,39),Gravity.CENTER);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);card.addView(title,new LinearLayout.LayoutParams(-1,dp(42)));
            card.setOnClickListener(v->openCode(index));GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=dp(142);lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);lp.setMargins(dp(6),dp(6),dp(6),dp(6));grid.addView(card,lp);
        }
        content.addView(grid);Button add=new Button(this);add.setText("+");add.setTextSize(30);add.setTextColor(Color.WHITE);add.setBackground(bg(Color.rgb(29,78,216),0,28));add.setOnClickListener(v->{int n=count()+1;prefs.edit().putInt("count",n).apply();selected=n-1;showEditor();});LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(58));ap.setMargins(dp(18),dp(6),dp(18),dp(22));content.addView(add,ap);scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void openCode(int i){String path=value("c",i);if(path.isEmpty()||!new File(path).exists()){Toast.makeText(this,"Nessun codice associato",Toast.LENGTH_SHORT).show();return;}shell(value("n",i).isEmpty()?"Codice":value("n",i));ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setImageURI(Uri.fromFile(new File(path)));image.setPadding(dp(18),dp(18),dp(18),dp(18));root.addView(image,new LinearLayout.LayoutParams(-1,0,1));Button back=new Button(this);back.setText("BACK");back.setOnClickListener(v->showHome());root.addView(back,new LinearLayout.LayoutParams(-1,dp(58)));}
    private String itemTitle(int i){String n=value("n",i).trim();return (i+1)+": "+(n.isEmpty()?"vuoto":n);}
    private String[] itemTitles(){String[] a=new String[count()];for(int i=0;i<a.length;i++)a[i]=itemTitle(i);return a;}

    private void showEditor(){
        shell("GESTISCI CONTENUTI");ScrollView scroll=new ScrollView(this);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),dp(14),dp(18),dp(30));
        box.addView(text("Scegli un elemento, assegna un nome e seleziona le immagini. La miniatura è facoltativa.",15,Color.DKGRAY,Gravity.LEFT));
        Spinner spinner=new Spinner(this);spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,itemTitles()));spinner.setSelection(Math.min(selected,count()-1));box.addView(spinner,new LinearLayout.LayoutParams(-1,dp(56)));
        labelField=new EditText(this);labelField.setHint("Nome visualizzato");labelField.setText(value("n",selected));box.addView(labelField,new LinearLayout.LayoutParams(-1,dp(60)));
        LinearLayout checks=new LinearLayout(this);checks.setOrientation(LinearLayout.VERTICAL);box.addView(checks);
        Button thumb=new Button(this);thumb.setText("SELEZIONA MINIATURA (OPZIONALE)");box.addView(thumb,new LinearLayout.LayoutParams(-1,dp(58)));
        Button code=new Button(this);code.setText("SELEZIONA QR / CODICE A BARRE");box.addView(code,new LinearLayout.LayoutParams(-1,dp(58)));
        Button save=new Button(this);save.setText("SALVA");save.setTextColor(Color.WHITE);save.setBackground(bg(Color.rgb(29,78,216),0,12));box.addView(save,new LinearLayout.LayoutParams(-1,dp(58)));
        Runnable refresh=()->updateChecks(checks);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){boolean first=true;public void onNothingSelected(android.widget.AdapterView<?>a){}public void onItemSelected(android.widget.AdapterView<?>a,View v,int pos,long id){if(first){first=false;selected=pos;}else{saveLabel();selected=pos;labelField.setText(value("n",pos));}refresh.run();}});
        thumb.setOnClickListener(v->{saveLabel();pickImage(PICK_THUMB);});code.setOnClickListener(v->{saveLabel();pickImage(PICK_CODE);});save.setOnClickListener(v->{saveLabel();Toast.makeText(this,"Elemento salvato",Toast.LENGTH_SHORT).show();showEditor();});refresh.run();scroll.addView(box);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }

    private void saveLabel(){if(labelField!=null)prefs.edit().putString("n"+selected,labelField.getText().toString().trim()).apply();}
    private void updateChecks(LinearLayout box){box.removeAllViews();box.addView(checkLine("Miniatura",value("t",selected)));box.addView(checkLine("Codice",value("c",selected)));}
    private TextView checkLine(String name,String path){boolean ok=!path.isEmpty()&&new File(path).exists();TextView v=text(name+": "+(ok?"impostata  ✓":"non impostata"),16,ok?Color.rgb(22,163,74):Color.DKGRAY,Gravity.LEFT);if(ok)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private void pickImage(int request){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,request);}
    private File imageFile(int index,boolean thumb){File dir=new File(getFilesDir(),"images");dir.mkdirs();return new File(dir,(thumb?"thumb_":"code_")+index+".img");}
    private void copy(InputStream in,File outFile)throws IOException{try(InputStream source=in;OutputStream out=new FileOutputStream(outFile)){byte[]b=new byte[8192];int n;while((n=source.read(b))>0)out.write(b,0,n);}}

    private void startExport(){saveLabel();Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/zip");i.putExtra(Intent.EXTRA_TITLE,"Q-Wallet-backup_1.3.zip");startActivityForResult(i,EXPORT_ZIP);}
    private void startImport(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/zip");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT_ZIP);}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;try{Uri uri=data.getData();if(request==PICK_THUMB||request==PICK_CODE){boolean thumb=request==PICK_THUMB;File f=imageFile(selected,thumb);copy(getContentResolver().openInputStream(uri),f);prefs.edit().putString((thumb?"t":"c")+selected,f.getAbsolutePath()).apply();showEditor();}else if(request==EXPORT_ZIP){exportZip(uri);Toast.makeText(this,"Esportazione completata",Toast.LENGTH_LONG).show();}else if(request==IMPORT_ZIP){importZip(uri);Toast.makeText(this,"Importazione completata",Toast.LENGTH_LONG).show();showHome();}}catch(Exception e){Toast.makeText(this,"Errore: "+e.getMessage(),Toast.LENGTH_LONG).show();}}

    private void exportZip(Uri uri)throws Exception{JSONArray items=new JSONArray();try(ZipOutputStream zip=new ZipOutputStream(getContentResolver().openOutputStream(uri))){for(int i=0;i<count();i++){JSONObject o=new JSONObject();o.put("name",value("n",i));o.put("thumb",addImage(zip,value("t",i),"images/thumb_"+i+".img"));o.put("code",addImage(zip,value("c",i),"images/code_"+i+".img"));items.put(o);}JSONObject m=new JSONObject();m.put("app","Q Wallet");m.put("version","1.3");m.put("items",items);zip.putNextEntry(new ZipEntry("manifest.json"));zip.write(m.toString(2).getBytes("UTF-8"));zip.closeEntry();}}
    private String addImage(ZipOutputStream zip,String path,String name)throws IOException{File f=new File(path);if(path.isEmpty()||!f.exists())return "";zip.putNextEntry(new ZipEntry(name));try(InputStream in=new FileInputStream(f)){byte[]b=new byte[8192];int n;while((n=in.read(b))>0)zip.write(b,0,n);}zip.closeEntry();return name;}
    private void importZip(Uri uri)throws Exception{File temp=new File(getCacheDir(),"import");deleteTree(temp);temp.mkdirs();try(ZipInputStream zip=new ZipInputStream(getContentResolver().openInputStream(uri))){ZipEntry e;while((e=zip.getNextEntry())!=null){File out=new File(temp,e.getName());if(!out.getCanonicalPath().startsWith(temp.getCanonicalPath()+File.separator))throw new IOException("Archivio non valido");if(e.isDirectory()){out.mkdirs();continue;}out.getParentFile().mkdirs();copy(zip,out);}}JSONObject m=new JSONObject(readFile(new File(temp,"manifest.json")));JSONArray a=m.getJSONArray("items");SharedPreferences.Editor ed=prefs.edit().clear().putInt("count",Math.max(MIN_ITEMS,a.length()));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);ed.putString("n"+i,o.optString("name"));String t=o.optString("thumb");if(!t.isEmpty()){File f=imageFile(i,true);copy(new FileInputStream(new File(temp,t)),f);ed.putString("t"+i,f.getAbsolutePath());}String c=o.optString("code");if(!c.isEmpty()){File f=imageFile(i,false);copy(new FileInputStream(new File(temp,c)),f);ed.putString("c"+i,f.getAbsolutePath());}}ed.apply();deleteTree(temp);}
    private String readFile(File f)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=new FileInputStream(f)){byte[]b=new byte[4096];int n;while((n=in.read(b))>0)out.write(b,0,n);}return out.toString("UTF-8");}
    private void deleteTree(File f){if(!f.exists())return;if(f.isDirectory()){File[] children=f.listFiles();if(children!=null)for(File c:children)deleteTree(c);}f.delete();}
    private void showInfo(){labelField=null;shell("INFORMAZIONI");root.addView(text("Archivio locale per i QR-CODE o Codici a Barre.\n\nLe immagini restano in locale sul dispositivo e l'app funziona offline.\n\nver. 1.3\n\nAutore Christian Marongiu + GPT-5.6 sol think.",17,Color.rgb(17,24,39),Gravity.CENTER),new LinearLayout.LayoutParams(-1,0,1));}
    @Override public void onBackPressed(){saveLabel();showHome();}
}
