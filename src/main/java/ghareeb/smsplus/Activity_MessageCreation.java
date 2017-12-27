package ghareeb.smsplus;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ghareeb.smsplus.Component_MessageCreator.OnSmiliesBarVisibilityChanged;
import ghareeb.smsplus.common.AppInfo;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.database.entities.SentMessage;
import ghareeb.smsplus.database.entities.ThreadEntity;
import ghareeb.smsplus.fragments.helper.FragmentListener;
import ghareeb.smsplus.guihelpers.MessageSendingActivity;
import ghareeb.smsplus.helper.SharedPreferencesHelper;

public class Activity_MessageCreation extends MessageSendingActivity implements FragmentListener {
    private EditText toET;
    private Component_MessageCreator creator;
    private boolean isContactSet = false;
    private String changedText;
    public static final String KEY_SHOW_PICK_CONTACT_IMMEDIATELY = AppInfo.PACKAGE + "key_show_pick_contact_immediately";
    private static final int EVENT_READ_CONTACT_PERMISSION_REQUIRED = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (clockValidated) {
            setContentView(R.layout.activity_message_creation);
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_bar_title_compose);
            toET = findViewById(R.id.toET);
            creator = findViewById(R.id.messageCreator);
            creator.setOnSendingListener(new Component_MessageCreator.OnSendingListener() {
                @Override
                public void sendRequested(SentMessage message) {
                    // TODO might need to do back stack stuff here
                    String number = message.getRecepient();
                    Intent chatIntent = new Intent(Activity_MessageCreation.this, Activity_Chat.class);
                    chatIntent.putExtra(Activity_Chat.KEY_CONTACT_PHONE_NUMBER, number);
                    chatIntent.putExtra(Activity_Chat.KEY_THREAD_ID, message.getThreadId());
                    startActivity(chatIntent);
                    finish();
                }
            });
            creator.setOnSmiliesVisibilityChangedListener(new OnSmiliesBarVisibilityChanged() {
                @Override
                public void visibilityChanged(boolean isVisible) {
                    boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

                    if (isVisible && isPortrait && lastDetectedPortKeyboardHeight > 0) {
                        creator.setSmiliesBarHeightInPixels(lastDetectedPortKeyboardHeight);
                    } else if (isVisible && !isPortrait && lastDetectedLandKeyboardHeight > 0) {
                        creator.setSmiliesBarHeightInPixels(lastDetectedLandKeyboardHeight);
                    } else {
                        creator.setSmiliesBarHeightToDefault();
                    }

                }
            });

            toET.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    changedText = s.subSequence(start, start + count).toString();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (isContactSet) {
                        isContactSet = false;
                        toET.setText(changedText);
                        toET.setSelection(changedText.length());
                    }

                    creator.setRecipient(toET.getText().toString());
                }
            });
            toET.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        if (creator != null)
                            creator.disableSmilies();
                    }

                }
            });

            Intent i = getIntent();

            if (i != null) {
                if (isImplicitIntent(i)) {
                    handleImplicitIntent(i);
                } else {
                    // Need to call this once only
                    if (savedInstanceState == null && i.getBooleanExtra(KEY_SHOW_PICK_CONTACT_IMMEDIATELY, false)) {
                        showPickContactActivity();
                    }
                }
            }
        }

    }

    private void showPickContactActivity() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    EVENT_READ_CONTACT_PERMISSION_REQUIRED);
        } else {
            performShowPickContactActivity();
        }
    }

    private void performShowPickContactActivity() {
        Intent intent = new Intent(this, Activity_PickContact.class);
        startActivityForResult(intent, 0);
    }

    private boolean isImplicitIntent(Intent i) {
        String action = i.getAction();

        if (action != null && action.length() > 0) {
            if (action.equals(Intent.ACTION_SEND) || action.equals(Intent.ACTION_SENDTO))
                return true;
        }

        return false;
    }

    private void handleImplicitIntent(Intent i) {
        String deviceNum = SharedPreferencesHelper.getUserPhoneNumber(this);

        // Check if device is registered
        if (deviceNum != null && deviceNum.length() > 0) {
            // Check if this intent is an sms-specific intent
            if (i.getScheme() != null && (i.getScheme().equalsIgnoreCase("sms") || i.getScheme().equalsIgnoreCase("smsto"))) {
                String body = i.getStringExtra("sms_body");

                if (i.getData() != null) {
                    String to = i.getData().getSchemeSpecificPart();

                    // Check if the address is existent and well-formed
                    if (to != null && PhoneNumberUtils.isWellFormedSmsAddress(to)) {
                        SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(this);
                        ThreadEntity thread = helper.Thread_findThreadOfPhoneNumber(to);

                        // Check if the address belongs to a registered contact
                        // Show the corresponding conversation
                        if (thread != null) {
                            Intent starter = new Intent(this, Activity_Chat.class);
                            starter.putExtra(Activity_Chat.KEY_CONTACT_PHONE_NUMBER, to);
                            starter.putExtra(Activity_Chat.KEY_THREAD_ID, thread.getId());
                            starter.putExtra(Activity_Chat.KEY_INITIAL_TEXT, body);
                            startActivity(starter);
                            finish();
                        } else {
                            // Show message creation activity filled with both
                            // body and address
                            toET.setText(to);
                            creator.setMessageText(body);
                        }
                    } else {
                        // Show message creation activity filled with body only
                        creator.setMessageText(body);
                    }
                } else {
                    // Show message creation activity filled with body only
                    creator.setMessageText(body);
                }
            } else {
                // Intent is not sms-specific so just extract the body and show
                // it
                String text = i.getStringExtra(Intent.EXTRA_TEXT);

                if (text == null || text.length() == 0) {
                    Uri u = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);

                    if (u != null) {
                        text = readFile(u.getPath());
                    }
                }

                creator.setMessageText(text);
            }

        } else {
            Toast.makeText(this, R.string.toast_number_not_registered, Toast.LENGTH_LONG).show();
        }

    }

    private String readFile(String path) {
        FileInputStream s = null;

        try {
            File f = new File(path);
            s = new FileInputStream(f);
            int length = (int) f.length();
            byte[] data = new byte[length];
            s.read(data);

            return new String(data);

        } catch (FileNotFoundException ignored) {
        } catch (IOException ignored) {
        } catch (Exception ignored) {

        } finally {
            if (s != null)
                try {
                    s.close();
                } catch (IOException ignored) {
                }
        }

        return "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        creator.initializeCounting();
    }

    @Override
    protected void onPause() {
        super.onPause();
        creator.stopCounting();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Check onCreate to see why
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_create_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showPickContactActivity();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        if (arg1 == Activity.RESULT_OK) {
            handleContactSelected(arg2);
        }
    }

    @Override
    public void onBackPressed() {
        if (creator != null && creator.isSmiliesBarShown())
            creator.setSmiliesBarShown(false);
        else
            super.onBackPressed();
    }

    private void handleContactSelected(Intent resultIntent) {
        String contactNumber = resultIntent.getStringExtra(Activity_PickContact.KEY_SELECTED_NUMBER);
        Contact c = new Contact(contactNumber, this);
        c.loadContactBasicContractInformation(this);
        String contactName = c.getName();
        String result;

        if (contactName.equals(contactNumber))
            result = contactNumber;
        else
            result = contactName + " (" + contactNumber + ")";

        toET.setText(result);
        isContactSet = true;
        creator.setRecipient(contactNumber);

    }

    @Override
    protected int getRootViewId() {
        return R.id.messageCreationActivityRoot;
    }

    //Added because of the subscription expiration dialog (fragment)
    @Override
    public void eventOccurred(int type, Object obj) {

    }

    @Override
    protected Component_MessageCreator getMessageCreator() {
        return creator;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EVENT_READ_CONTACT_PERMISSION_REQUIRED:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    performShowPickContactActivity();
                } else {
                    Log.e(Activity_RegistrationProgress.class.getName(), "No permission to read contacts");
                }
                break;
        }
    }
}
