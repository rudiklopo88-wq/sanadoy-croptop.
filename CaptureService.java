package id.aibelajar.app;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import java.io.*;

public class CaptureService extends Service {
    public static final String ACTION_CAPTURED="id.aibelajar.app.CAPTURED";
    private MediaProjection projection;
    private ImageReader reader;
    private VirtualDisplay display;
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        createNotification();
        int code=intent.getIntExtra("resultCode",0);
        Intent data=intent.getParcelableExtra("data");
        MediaProjectionManager mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        projection=mgr.getMediaProjection(code,data);
        if(projection==null){ stopSelf(); return START_NOT_STICKY; }
        projection.registerCallback(new MediaProjection.Callback(){ @Override public void onStop(){ cleanup(); stopSelf(); } },new Handler());
        int w=getResources().getDisplayMetrics().widthPixels, h=getResources().getDisplayMetrics().heightPixels, density=getResources().getDisplayMetrics().densityDpi;
        reader=ImageReader.newInstance(w,h,PixelFormat.RGBA_8888,2);
        display=projection.createVirtualDisplay("Sanadoy croptop Capture",w,h,density,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,null);
        reader.setOnImageAvailableListener(r -> {
            Image image=null;
            try {
                image=r.acquireLatestImage();
                if(image==null) return;
                Image.Plane p=image.getPlanes()[0]; android.graphics.Rect crop=image.getCropRect();
                int pixelStride=p.getPixelStride(), rowStride=p.getRowStride();
                Bitmap full=Bitmap.createBitmap(rowStride/pixelStride,image.getHeight(),Bitmap.Config.ARGB_8888);
                full.copyPixelsFromBuffer(p.getBuffer());
                Bitmap bmp=Bitmap.createBitmap(full,crop.left,crop.top,crop.width(),crop.height());
                File out=new File(getCacheDir(),"screen_capture.png");
                try(FileOutputStream fos=new FileOutputStream(out)){ bmp.compress(Bitmap.CompressFormat.PNG,100,fos); }
                Intent done=new Intent(ACTION_CAPTURED); done.setPackage(getPackageName()); done.putExtra("path",out.getAbsolutePath()); sendBroadcast(done);
                bmp.recycle(); full.recycle();
                cleanup(); stopSelf();
            } catch(Exception e){ Intent err=new Intent(ACTION_CAPTURED); err.setPackage(getPackageName()); err.putExtra("error",e.getMessage()); sendBroadcast(err); cleanup(); stopSelf(); }
            finally { if(image!=null) image.close(); }
        },new Handler());
        return START_NOT_STICKY;
    }
    private void createNotification(){
        String channel="capture";
        if(Build.VERSION.SDK_INT>=26){ NotificationChannel c=new NotificationChannel(channel,"Screen capture",NotificationManager.IMPORTANCE_LOW); ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c); }
        Notification n=new Notification.Builder(this,channel).setContentTitle("Sanadoy croptop").setContentText("Sedang mengambil layar atas izin Anda").setSmallIcon(android.R.drawable.ic_menu_camera).build();
        if(Build.VERSION.SDK_INT>=29) startForeground(7,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION); else startForeground(7,n);
    }
    private void cleanup(){ try{if(display!=null)display.release();}catch(Exception ignored){} try{if(reader!=null)reader.close();}catch(Exception ignored){} try{if(projection!=null)projection.stop();}catch(Exception ignored){} }
    @Override public void onDestroy(){cleanup();super.onDestroy();}
    @Override public android.os.IBinder onBind(Intent i){return null;}
}
