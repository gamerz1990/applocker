import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.maliks.applocker.xtreme.service.AppLockerService

class BootCompleteReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootCompleteReceiver", "Boot completed. Starting AppLockerService.")
            
            val serviceIntent = Intent(context, AppLockerService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}