package fantaq.fanta.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import trikita.jedux.Action;
import fantaq.fanta.Actions;
import fantaq.fanta.App;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        App.dispatch(new Action<>(Actions.Alarm.WAKEUP));
    }
}
