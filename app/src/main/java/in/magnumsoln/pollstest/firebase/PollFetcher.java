package in.magnumsoln.pollstest.firebase;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import in.magnumsoln.pollstest.interfaces.FinishFetchingDataCallback;
import in.magnumsoln.pollstest.model.Poll;

public class PollFetcher {
    private FirebaseFirestore db;
    private List<Poll> polls;

    public PollFetcher() {
        db = FirebaseFirestore.getInstance();
        polls = new ArrayList<>();
    }

    private final Date currentDate = Calendar.getInstance().getTime();

    public void fetchLatestPolls(final FinishFetchingDataCallback finishFetchingData) {
        try {
            db.collection("POLL")
                    .whereLessThanOrEqualTo("START_TIME", currentDate)
                    .orderBy("START_TIME", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            if (documentSnapshots.isEmpty()) {
                                finishFetchingData.onFailed("No questions", "POLL");
                                return;
                            } else {
                                List<DocumentSnapshot> documentsList = documentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : documentsList) {
                                    if(polls.size() == 5)   // limit to only 5 latest open polls
                                        break;
                                    Timestamp start = (Timestamp) documentSnapshot.get("START_TIME");
                                    Timestamp close = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                                    Timestamp declare = (Timestamp) documentSnapshot.get("DECLARE_TIME");
                                    if ((close == null) || (close.toDate().after(currentDate))) {
                                        Date start_ = start.toDate();
                                        Date close_ = (close == null) ? null : close.toDate();
                                        Date declare_ = (declare == null) ? null : declare.toDate();
                                        long correct_option = -1;
                                        if (documentSnapshot.get("CORRECT_OPTION") != null)
                                            correct_option = (long) documentSnapshot.get("CORRECT_OPTION");
                                        polls.add(new Poll(
                                                documentSnapshot.getId(),
                                                (String) documentSnapshot.get("QUESTION"),
                                                (String) documentSnapshot.get("TOPIC"),
                                                start_,
                                                close_,
                                                declare_,
                                                "OPEN",
                                                (String) documentSnapshot.get("IMAGE_URL"),
                                                (String) documentSnapshot.get("SHARE_URL"),
                                                (List<String>) documentSnapshot.get("OPTIONS"),
                                                correct_option,
                                                (long) documentSnapshot.get("REWARD_AMOUNT")
                                        ));
                                    }
                                }
                                finishFetchingData.onFinish(polls, "POLL");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finishFetchingData.onFailed(null, "POLL");
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void fetchActivePolls(final FinishFetchingDataCallback finishFetchingDataCallback, String topic) {
        try {
            db.collection("POLL")
                    .whereEqualTo("TOPIC", topic)
                    .whereLessThanOrEqualTo("START_TIME", currentDate)
                    .orderBy("START_TIME", Query.Direction.ASCENDING).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.isEmpty()) {
                                finishFetchingDataCallback.onFailed("No polls available", "POLL");
                                return;
                            } else {
                                List<DocumentSnapshot> documentsList = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : documentsList) {
                                    Timestamp start = (Timestamp) documentSnapshot.get("START_TIME");
                                    Timestamp close = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                                    Timestamp declare = (Timestamp) documentSnapshot.get("DECLARE_TIME");
                                    if ((close == null) || (close.toDate().after(currentDate))) {
                                        Date start_ = start.toDate();
                                        Date close_ = (close == null) ? null : close.toDate();
                                        Date declare_ = (declare == null) ? null : declare.toDate();
                                        long correct_option = -1;
                                        if (documentSnapshot.get("CORRECT_OPTION") != null)
                                            correct_option = (long) documentSnapshot.get("CORRECT_OPTION");
                                        polls.add(new Poll(
                                                documentSnapshot.getId(),
                                                (String) documentSnapshot.get("QUESTION"),
                                                (String) documentSnapshot.get("TOPIC"),
                                                start_,
                                                close_,
                                                declare_,
                                                "OPEN",
                                                (String) documentSnapshot.get("IMAGE_URL"),
                                                (String) documentSnapshot.get("SHARE_URL"),
                                                (List<String>) documentSnapshot.get("OPTIONS"),
                                                correct_option,
                                                (long) documentSnapshot.get("REWARD_AMOUNT")
                                        ));
                                    }
                                }
                                finishFetchingDataCallback.onFinish(polls, "POLL");
                            }
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void fetchClosedPolls(final FinishFetchingDataCallback finishFetchingDataCallback, String topic) {
        try {
            db.collection("POLL")
                    .whereEqualTo("TOPIC", topic)
                    .whereLessThanOrEqualTo("CLOSE_TIME", currentDate)
                    .orderBy("CLOSE_TIME", Query.Direction.ASCENDING).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.isEmpty()) {
                                finishFetchingDataCallback.onFailed("No polls available", "POLL");
                                return;
                            } else {
                                List<DocumentSnapshot> documentsList = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : documentsList) {
                                    Timestamp start = (Timestamp) documentSnapshot.get("START_TIME");
                                    Timestamp close = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                                    Timestamp declare = (Timestamp) documentSnapshot.get("DECLARE_TIME");
                                    if ((declare == null) || (declare.toDate().after(currentDate))) {
                                        Date start_ = start.toDate();
                                        Date close_ = close.toDate();
                                        Date declare_ = (declare == null) ? null : declare.toDate();
                                        long correct_option = -1;
                                        if (documentSnapshot.get("CORRECT_OPTION") != null)
                                            correct_option = (long) documentSnapshot.get("CORRECT_OPTION");
                                        polls.add(new Poll(
                                                documentSnapshot.getId(),
                                                (String) documentSnapshot.get("QUESTION"),
                                                (String) documentSnapshot.get("TOPIC"),
                                                start_,
                                                close_,
                                                declare_,
                                                "CLOSED",
                                                (String) documentSnapshot.get("IMAGE_URL"),
                                                (String) documentSnapshot.get("SHARE_URL"),
                                                (List<String>) documentSnapshot.get("OPTIONS"),
                                                correct_option,
                                                (long) documentSnapshot.get("REWARD_AMOUNT")
                                        ));
                                    }
                                }
                                finishFetchingDataCallback.onFinish(polls, "POLL");
                            }
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void fetchDeclaredPolls(final FinishFetchingDataCallback finishFetchingDataCallback, String topic) {
        try {
            Date currentDate = Calendar.getInstance().getTime();
            Date minDate = new Date(currentDate.getTime() - 864000000); // DECLARE date for last 10 days
            System.out.println("minDate : " + minDate);
            db.collection("POLL")
                    .whereEqualTo("TOPIC", topic)
                    .whereLessThanOrEqualTo("DECLARE_TIME", currentDate)
                    .whereGreaterThanOrEqualTo("DECLARE_TIME", minDate)
                    .orderBy("DECLARE_TIME", Query.Direction.ASCENDING).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.isEmpty()) {
                                finishFetchingDataCallback.onFailed("No polls available", "POLL");
                                return;
                            } else {
                                List<DocumentSnapshot> documentsList = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : documentsList) {
                                    Timestamp start = (Timestamp) documentSnapshot.get("START_TIME");
                                    Timestamp close = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                                    Timestamp declare = (Timestamp) documentSnapshot.get("DECLARE_TIME");
                                    polls.add(new Poll(
                                            documentSnapshot.getId(),
                                            (String) documentSnapshot.get("QUESTION"),
                                            (String) documentSnapshot.get("TOPIC"),
                                            start.toDate(),
                                            close.toDate(),
                                            declare.toDate(),
                                            "DECLARED",
                                            (String) documentSnapshot.get("IMAGE_URL"),
                                            (String) documentSnapshot.get("SHARE_URL"),
                                            (List<String>) documentSnapshot.get("OPTIONS"),
                                            (long) documentSnapshot.get("CORRECT_OPTION"),
                                            (long) documentSnapshot.get("REWARD_AMOUNT")
                                    ));
                                }
                                finishFetchingDataCallback.onFinish(polls, "POLL");
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
