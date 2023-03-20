package com.example.fieldgenie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class SmsBroadcast extends BroadcastReceiver {

    public SmsBroadcastListener smsBroadcastListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction() == SmsRetriever.SMS_RETRIEVED_ACTION){
            Bundle extras = intent.getExtras();

            Status smsStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            switch(smsStatus.getStatusCode()){

                case CommonStatusCodes
                        .SUCCESS:
                    Intent messageIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                    smsBroadcastListener.onSuccess(messageIntent);
                    break;

                case CommonStatusCodes.TIMEOUT:
                    smsBroadcastListener.onFailure();
                    break;
            }
        }

    }
    public interface SmsBroadcastListener{
        void onSuccess(Intent intent);
        void onFailure();
    }
}
