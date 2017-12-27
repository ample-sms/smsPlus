package ghareeb.smsplus;

import ghareeb.smsplus.fragments.Dialog_SendVerification;
import ghareeb.smsplus.fragments.Fragment_RegistrationProgress;
import ghareeb.smsplus.fragments.helper.FragmentListener;
import ghareeb.smsplus.fragments.parents.Dialog_OkParent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class Activity_RegistrationProgress extends AppCompatActivity implements FragmentListener {
    public static final String KEY_PHONE_NUMBER = "keyPhoneNumber";
    public static final String KEY_COUNTRY_CODE = "keyCountryCode";
    public static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_progress);

        Intent starter = getIntent();
        String phoneNumber = starter.getStringExtra(KEY_PHONE_NUMBER);
        String countryCode = starter.getStringExtra(KEY_COUNTRY_CODE);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment_RegistrationProgress fragment = (Fragment_RegistrationProgress) fragmentManager
                .findFragmentById(R.id.registrationProgressFragmentContainer);

        if (fragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragment = new Fragment_RegistrationProgress(phoneNumber, countryCode);
            fragmentTransaction.add(R.id.registrationProgressFragmentContainer, fragment);
            fragmentTransaction.commit();
        }
    }


    @Override
    public void eventOccurred(int type, Object obj) {
        switch (type) {
            case Fragment_RegistrationProgress.EVENT_NO_INTERNET_CONNECTION:
                Dialog_OkParent dialog1 = new Dialog_OkParent();
                dialog1.setTexts(getString(R.string.dialog_no_internet_title), getString(R.string.dialog_no_internet_body), getString(R.string.ok));
                dialog1.show(getSupportFragmentManager(), "no internet dialog");
                break;
            case Fragment_RegistrationProgress.EVENT_VERIFICATION_FAILED:
                Dialog_OkParent dialog2 = new Dialog_OkParent();
                dialog2.setTexts(getString(R.string.register_failed), getString(R.string.register_verification_failed), getString(R.string.ok));
                dialog2.show(getSupportFragmentManager(), "verification failed dialog");
                break;
            case Fragment_RegistrationProgress.EVENT_INTERNET_ERROR:
                Dialog_OkParent dialog3 = new Dialog_OkParent();
                dialog3.setTexts(getString(R.string.register_failed), getString(R.string.internet_fail), getString(R.string.ok));
                dialog3.show(getSupportFragmentManager(), "internet error dialog");
                break;
            case Fragment_RegistrationProgress.EVENT_VERIFICATION_REQUIRED:
                Dialog_SendVerification dialog = new Dialog_SendVerification();
                dialog.show(getSupportFragmentManager(), "verify dialog");
                break;
            case Fragment_RegistrationProgress.EVENT_READ_CONTACT_PERMISSION_REQUIRED:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACT);
                break;
            case Dialog_SendVerification.EVENT_CANCEL_PRESSED:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case Fragment_RegistrationProgress.EVENT_REGISTRATION_SUCCESSFUL:
                Dialog_OkParent dialog4 = new Dialog_OkParent();
                StringBuilder bodyBuilder = new StringBuilder();
                bodyBuilder.append(getString(R.string.register_ok_description));
                bodyBuilder.append("\n");

                if (((Integer) obj) > 0)//there still are days in the subscription
                    bodyBuilder.append(String.format(getString(R.string.subscription_remaining_days), (Integer) obj));
                else
                    bodyBuilder.append(getString(R.string.subscription_expired_description));

                dialog4.setTexts(getString(R.string.register_ok_title), bodyBuilder.toString(), getString(R.string.ok));

                dialog4.show(getSupportFragmentManager(), "registration successful dialog");

                break;
            case Dialog_SendVerification.EVENT_YES_PRESSED:
                //Request for permission first!
                int permissionCheck = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.SEND_SMS);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {//We already have the permission
                    Fragment f = getSupportFragmentManager().findFragmentById(R.id.registrationProgressFragmentContainer);

                    if (f != null) {
                        ((Fragment_RegistrationProgress) f).confirmVerificationRequest();
                    }
                }

                break;
            case Dialog_OkParent.EVENT_OK_PRESSED:
                Fragment ff = getSupportFragmentManager().findFragmentById(R.id.registrationProgressFragmentContainer);

                if (((Fragment_RegistrationProgress) ff).hasFailed())
                    setResult(Activity.RESULT_CANCELED);
                else
                    setResult(Activity.RESULT_OK);

                finish();
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Fragment f = getSupportFragmentManager().findFragmentById(R.id.registrationProgressFragmentContainer);

                    if (f != null) {
                        ((Fragment_RegistrationProgress) f).confirmVerificationRequest();
                    }
                } else {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
                break;
            case MY_PERMISSIONS_REQUEST_READ_CONTACT:
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.registrationProgressFragmentContainer);
                boolean result = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                ((Fragment_RegistrationProgress) f).informResultOfReadContactPermissionRequest(result);
                break;

        }
    }
}
