package id.aibelajar.app;

import android.app.*;
import android.content.*;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends ComponentActivity {
    private ActivityResultLauncher<Intent> captureLauncher;
    private EditText endpoint;
    private android.content.BroadcastReceiver captureReceiver;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout layout = new LinearLayout(this); layout.setOrientation(1); layout.setPadding(28,36,28,24);
        layout.setBackgroundColor(0xFF101827);
        TextView title = new TextView(this); title.setText("Sanadoy croptop"); title.setTextSize(28); title.setTextColor(0xFFFFFFFF); title.setGravity(17);
        TextView sub = new TextView(this); sub.setText("Crop soal langsung dari layar HP"); sub.setTextColor(0xFFB8C4D8); sub.setGravity(17);
        endpoint = new EditText(this); endpoint.setSingleLine(true); endpoint.setTextColor(0xFFFFFFFF); endpoint.setHintTextColor(0xFF9AA8BC);
        endpoint.setHint("URL backend, contoh https://domain-anda.com");
        endpoint.setText(getPreferences(0).getString("endpoint",""));
        Button save = button("SIMPAN URL BACKEND");
        Button crop = button("CROP LAYAR");
        TextView info = new TextView(this); info.setTextColor(0xFFB8C4D8); info.setText("Cara pakai: buka halaman soal, kembali ke Sanadoy croptop, tekan CROP LAYAR, izinkan berbagi layar, lalu tandai area soal.");
        layout.addView(title); layout.addView(sub); layout.addView(endpoint); layout.addView(save); layout.addView(crop); layout.addView(info);
        setContentView(layout);
        captureReceiver = new android.content.BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String path=intent.getStringExtra("path");
                String error=intent.getStringExtra("error");
                if(path!=null) {
                    Intent next=new Intent(MainActivity.this,CropActivity.class);
                    next.putExtra("path",path);
                    next.putExtra("endpoint",endpoint.getText().toString().trim());
                    startActivity(next);
                } else Toast.makeText(MainActivity.this,"Gagal mengambil layar: "+error,Toast.LENGTH_LONG).show();
            }
        };
        IntentFilter filter=new IntentFilter(CaptureService.ACTION_CAPTURED);
        if(Build.VERSION.SDK_INT>=33) registerReceiver(captureReceiver,filter,Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(captureReceiver,filter);
        save.setOnClickListener(v -> {
            getPreferences(0).edit().putString("endpoint", endpoint.getText().toString().trim()).apply();
            Toast.makeText(this,"URL disimpan",Toast.LENGTH_SHORT).show();
        });
        crop.setOnClickListener(v -> {
            String url=endpoint.getText().toString().trim();
            if(url.isEmpty()){ endpoint.setError("Masukkan URL backend terlebih dahulu"); return; }
            getPreferences(0).edit().putString("endpoint",url).apply();
            MediaProjectionManager m=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
            captureLauncher.launch(m.createScreenCaptureIntent());
        });
        captureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode()!=RESULT_OK || result.getData()==null){ Toast.makeText(this,"Izin tangkapan layar dibatalkan",Toast.LENGTH_LONG).show(); return; }
            Intent svc=new Intent(this,CaptureService.class);
            svc.putExtra("resultCode",result.getResultCode()); svc.putExtra("data",result.getData());
            if(Build.VERSION.SDK_INT>=26) startForegroundService(svc); else startService(svc);
            Toast.makeText(this,"Mengambil layar…",Toast.LENGTH_SHORT).show();
        });
    }
    @Override protected void onDestroy(){ if(captureReceiver!=null) unregisterReceiver(captureReceiver); super.onDestroy(); }\n    private Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextColor(0xFFFFFFFF); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6759E8)); return b; }
}
