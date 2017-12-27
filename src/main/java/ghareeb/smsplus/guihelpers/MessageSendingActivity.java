package ghareeb.smsplus.guihelpers;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import ghareeb.smsplus.Component_MessageCreator;

/**
 * Handles common tasks required by activities responsible for sending an sms
 */
public abstract class MessageSendingActivity extends SoftKeyboardMonitoringActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    public void initiateSendMessage() {
        //Request for permission first!
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }else{
            getMessageCreator().sendMessage();
        }
    }

    protected abstract Component_MessageCreator getMessageCreator();

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getMessageCreator().sendMessage();
                }
                break;
        }
    }
}
