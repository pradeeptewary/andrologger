package com.andrologger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;


/**
 * A login screen that offers login via email/password.
 */
public class SendServer extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_server);
        AlertDialog dialogBox = makeAndShowDialogBox();
        dialogBox.show();
    }

    private AlertDialog makeAndShowDialogBox() {
        // Get text values for user consent banner
        String consentBannerText = getString(R.string.user_popup_text);
        String consentBannerTitle = getString(R.string.user_popup_title);
        // Pop up consent dialog
        AlertDialog myDialogBox =
                new AlertDialog.Builder(this)
                        .setTitle(consentBannerTitle)
                        .setMessage(consentBannerText)
                        .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }
                        })
                        .create();
        return myDialogBox;
    }
}

