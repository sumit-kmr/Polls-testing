package in.magnumsoln.poll.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import in.magnumsoln.poll.R;
import in.magnumsoln.poll.activity.MainActivity;
import in.magnumsoln.poll.activity.PollActivity;
import in.magnumsoln.poll.model.Poll;

public class NotificationService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        System.out.println("Notification received................");
        String pollId = remoteMessage.getData().get("pollId");
        FirebaseFirestore.getInstance().collection("POLL")
                .document(pollId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Poll poll = new Poll(documentSnapshot.getId(),
                        (String) documentSnapshot.get("QUESTION"),
                        (String) documentSnapshot.get("TOPIC"),
                        null,
                        null,
                        null,
                        "OPEN" ,
                        (String) documentSnapshot.get("IMAGE_URL"),
                        (String) documentSnapshot.get("SHARE_URL"),
                        (List<String>) documentSnapshot.get("OPTIONS"),
                        (long) documentSnapshot.get("CORRECT_OPTION"),
                        (long) documentSnapshot.get("REWARD_AMOUNT"));
                showNotification(poll);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void showNotification(Poll poll) {
        Intent intent = new Intent(this, PollActivity.class);
        intent.putExtra("poll",poll);
        intent.putExtra("poll_status","OPEN");
        intent.setAction("openPoll");
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"notification")
                .setSmallIcon(R.drawable.tick)
                .setContentTitle("New poll available!!")
                .setContentText("Tap to open the poll")
                .setColor(getColor(R.color.colorPrimaryDark))
                .setContentIntent(pi)
                .setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());
    }

}
