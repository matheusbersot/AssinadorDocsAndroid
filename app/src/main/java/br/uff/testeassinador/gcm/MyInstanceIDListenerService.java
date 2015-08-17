package br.uff.testeassinador.gcm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by matheus on 06/08/15.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = MyInstanceIDListenerService.class.getName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {

        //Dizer que o novo token ainda não foi enviado ao servidor
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean(QuickStartPreferences.SENT_TOKEN_TO_SERVER, false).apply();

        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
    // [END refresh_token]
}
