package id.aibelajar.app;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class CropActivity extends Activity {
    private CropView cropView; private TextView result; private String imagePath, endpoint;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        imagePath=getIntent().getStringExtra("path");
        endpoint=getIntent().getStringExtra("endpoint"); if(endpoint==null) endpoint="";
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(12,12,12,12);
        cropView=new CropView(this); cropView.setImage(BitmapFactory.decodeFile(imagePath));
        Button send=new Button(this); send.setText("KIRIM KE AI");
        result=new TextView(this); result.setText("Geser jari untuk memilih area soal."); result.setTextSize(16);
        ScrollView scroll=new ScrollView(this); scroll.addView(result);
        root.addView(cropView,new LinearLayout.LayoutParams(-1,0,1)); root.addView(send); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        send.setOnClickListener(v->{ Bitmap selected=cropView.getCropped(); if(selected==null){result.setText("Pilih area soal terlebih dahulu.");return;} result.setText("AI sedang membaca soal…"); new AnalyzeTask(selected).execute(); });
    }
    class AnalyzeTask extends AsyncTask<Void,Void,String>{
        Bitmap bitmap; AnalyzeTask(Bitmap b){bitmap=b;}
        protected String doInBackground(Void...v){
            try{
                String base=endpoint.replaceAll("/+$","");
                URL url=new URL(base+"/solve"); HttpURLConnection c=(HttpURLConnection)url.openConnection();
                c.setRequestMethod("POST"); c.setDoOutput(true); c.setConnectTimeout(20000); c.setReadTimeout(60000);
                String boundary="----AIBelajarBoundary";
                c.setRequestProperty("Content-Type","multipart/form-data; boundary="+boundary);
                ByteArrayOutputStream bytes=new ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.JPEG,90,bytes);
                try(DataOutputStream out=new DataOutputStream(c.getOutputStream())){
                    out.writeBytes("--"+boundary+"\r\nContent-Disposition: form-data; name=\"image\"; filename=\"question.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n");
                    out.write(bytes.toByteArray()); out.writeBytes("\r\n--"+boundary+"--\r\n");
                }
                InputStream is=(c.getResponseCode()<400)?c.getInputStream():c.getErrorStream();
                ByteArrayOutputStream response=new ByteArrayOutputStream(); byte[] buf=new byte[4096]; int n; while((n=is.read(buf))>0)response.write(buf,0,n);
                String body=response.toString("UTF-8"); c.disconnect();
                org.json.JSONObject obj=new org.json.JSONObject(body); return obj.optString("answer",body);
            }catch(Exception e){return "Gagal: "+e.getMessage()+"\nPeriksa URL backend dan koneksi internet.";}
        }
        protected void onPostExecute(String s){result.setText(s);bitmap.recycle();}
    }
    class CropView extends View {
        Bitmap bmp; Paint paint=new Paint(3); float sx,sy,ox,oy; float x1,y1,x2,y2; boolean selected=false;
        CropView(Context c){super(c);} void setImage(Bitmap b){bmp=b;invalidate();}
        protected void onDraw(Canvas c){super.onDraw(c); if(bmp==null)return;
            float scale=Math.min((float)getWidth()/bmp.getWidth(),(float)getHeight()/bmp.getHeight());
            sx=sy=scale; ox=(getWidth()-bmp.getWidth()*scale)/2; oy=(getHeight()-bmp.getHeight()*scale)/2;
            c.drawColor(Color.BLACK); c.drawBitmap(bmp,null,new RectF(ox,oy,ox+bmp.getWidth()*scale,oy+bmp.getHeight()*scale),paint);
            if(selected){paint.setColor(0x5533B5E5); c.drawRect(x1,y1,x2,y2,paint); paint.setColor(Color.GREEN); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(4); c.drawRect(x1,y1,x2,y2,paint); paint.setStyle(Paint.Style.FILL); paint.setColor(Color.WHITE);}
        }
        public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX(),y=e.getY(); if(e.getAction()==0){x1=x2=x;y1=y2=y;selected=true;invalidate();return true;} if(e.getAction()==1||e.getAction()==2){x2=x;y2=y;invalidate();return true;} return true;}
        Bitmap getCropped(){if(!selected||bmp==null)return null;
            int l=(int)Math.max(0,(Math.min(x1,x2)-ox)/sx), t=(int)Math.max(0,(Math.min(y1,y2)-oy)/sy);
            int r=(int)Math.min(bmp.getWidth(),(Math.max(x1,x2)-ox)/sx), b=(int)Math.min(bmp.getHeight(),(Math.max(y1,y2)-oy)/sy);
            if(r-l<20||b-t<20)return null; return Bitmap.createBitmap(bmp,l,t,r-l,b-t);
        }
    }
}
