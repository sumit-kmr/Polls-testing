package in.magnumsoln.pollstest.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.activity.PollActivity;
import in.magnumsoln.pollstest.activity.SplashActivity;
import in.magnumsoln.pollstest.model.Poll;

public class NotificationService extends FirebaseMessagingService {
    String status = "", title = "", details = "";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try {
            super.onMessageReceived(remoteMessage);
            System.out.println("Notification received...");
            final Map<String, String> data = remoteMessage.getData();
            String landingPage = data.get("landingPage");
            // land on poll page
            if (landingPage.equalsIgnoreCase("poll")) {
                String pollId = data.get("pollId");
                final String notificationType = data.get("type");
                FirebaseFirestore.getInstance().collection("POLL")
                        .document(pollId)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Timestamp start_time = (Timestamp) documentSnapshot.get("START_TIME");
                        Timestamp close_time = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                        Timestamp declare_time = (Timestamp) documentSnapshot.get("DECLARE_TIME");
                        Date start_ = (start_time == null) ? null : start_time.toDate();
                        Date close_ = (close_time == null) ? null : close_time.toDate();
                        Date declare_ = (declare_time == null) ? null : declare_time.toDate();
                        Date current_time = Calendar.getInstance().getTime();
                        if (start_ == null)
                            return;
                        if (start_.after(current_time))
                            return;
                        if (close_time == null || close_time.toDate().after(current_time)) {
                            status = "OPEN";
                        } else if (close_time.toDate().before(current_time)) {
                            if (declare_time == null || declare_time.toDate().after(current_time))
                                status = "CLOSED";
                            else if (declare_time.toDate().before(current_time)) {
                                status = "DECLARED";
                            }
                        }
                        long correct_option = 99, reward_amount = 4;
                        if (documentSnapshot.get("CORRECT_OPTION") != null) {
                            correct_option = (long) documentSnapshot.get("CORRECT_OPTION");
                        }
                        if (documentSnapshot.get("REWARD_AMOUNT") != null) {
                            reward_amount = (long) documentSnapshot.get("REWARD_AMOUNT");
                        }
                        String imageUrl = (String) documentSnapshot.get("IMAGE_URL");
                        final Poll poll = new Poll(documentSnapshot.getId(),
                                (String) documentSnapshot.get("QUESTION"),
                                (String) documentSnapshot.get("TOPIC"),
                                start_,
                                close_,
                                declare_,
                                status,
                                imageUrl,
                                (String) documentSnapshot.get("SHARE_URL"),
                                (List<String>) documentSnapshot.get("OPTIONS"),
                                correct_option,
                                reward_amount);
                        if (notificationType.equalsIgnoreCase("newPoll")) {
                            title = poll.getQUESTION();
                            details = "Vote now";
                        } else {
                            title = data.get("title");
                            details = data.get("detail");
                        }
                        // load notification large image and show notification
                        Glide.with(NotificationService.this)
                                .load(imageUrl)
                                .into(new SimpleTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                        showPollNotification(poll,status,title,details,bitmap);
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }
            // land on topic page
            else if (landingPage.equalsIgnoreCase("topic")) {
                final String topicName = data.get("topicName");
                final String title = data.get("title");
                final String detail = data.get("detail");
                FirebaseFirestore.getInstance().collection("TOPIC").document(topicName).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    final String shareUrl = (String) documentSnapshot.get("SHARE_URL");
                                    String imageUrl = (String) documentSnapshot.get("IMAGE_URL");
                                    // load notification large image and show notification
                                    Glide.with(NotificationService.this)
                                            .load(imageUrl)
                                            .into(new SimpleTarget<Drawable>() {
                                                @Override
                                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                                    showTopicNotification(topicName,shareUrl,title,detail,bitmap);
                                                }
                                            });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
            }
            // land on dashboard
            else if (landingPage.equalsIgnoreCase("dashboard")) {
                String title = data.get("title");
                String detail = data.get("detail");
                showDashboardNotification(title, detail);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDashboardNotification(String title, String detail) {
        try {
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setAction("openDashboard" + System.currentTimeMillis());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notification")
                    .setContentTitle(title)
                    .setContentText(detail)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pi)
                    .setAutoCancel(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            int randomId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            notificationManager.notify(randomId, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showTopicNotification(String topicName, String shareUrl, String title, String detail, Bitmap bitmap) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("category_name", topicName);
            bundle.putString("topic_share_url", shareUrl);
            Intent intent = new Intent(this, SplashActivity.class);
            intent.putExtra("fromNotification", true);
            intent.putExtra("type", "topic");
            intent.putExtra("bundle", bundle);
            intent.setAction("openTopic" + System.currentTimeMillis());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notification")
                    .setContentTitle(title)
                    .setContentText(detail)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setLargeIcon(bitmap)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null));
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            int randomId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            notificationManager.notify(randomId, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPollNotification(Poll poll, String status, String title, String details, Bitmap bitmap) {
        try {
            Bundle bundle = new Bundle();
            bundle.putSerializable("poll", poll);
            bundle.putString("poll_status", status);
            Intent intent = new Intent(this, SplashActivity.class);
            intent.putExtra("fromNotification", true);
            intent.putExtra("type", "poll");
            intent.putExtra("bundle", bundle);
            intent.setAction("openPoll" + System.currentTimeMillis());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notification")
                    .setContentTitle(title)
                    .setContentText(details)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setLargeIcon(bitmap)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null));
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            int randomId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            notificationManager.notify(randomId, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
