package in.magnumsoln.poll.firebase;

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
import in.magnumsoln.poll.interfaces.FinishFetchingDataCallback;
import in.magnumsoln.poll.model.Poll;

public class PollFetcher {
    private FirebaseFirestore db;
    private List<Poll> polls;

    public PollFetcher() {
        db = FirebaseFirestore.getInstance();
        polls = new ArrayList<>();
    }

    final Date currentDate = Calendar.getInstance().getTime();

    public void fetchLatestPolls(final FinishFetchingDataCallback finishFetchingData) {
        db.collection("POLL")
                .whereLessThanOrEqualTo("START_TIME",currentDate)
                .orderBy("START_TIME", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            finishFetchingData.onFailed("No questions", "POLL");
                            return;
                        } else {
                            List<DocumentSnapshot> documentsList = documentSnapshots.getDocuments();
                            for(DocumentSnapshot documentSnapshot : documentsList){
                                Timestamp start = (Timestamp) documentSnapshot.get("START_TIME");
                                Timestamp close = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                                Timestamp declare = (Timestamp) documentSnapshot.get("DECLARE_TIME");
                                if((close==null) || (close.toDate().after(currentDate))) {
                                    Date start_ = start.toDate();
                                    Date close_ = (close == null) ? null : close.toDate();
                                    Date declare_ = (declare == null) ? null : declare.toDate();
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
                                            (long) documentSnapshot.get("CORRECT_OPTION"),
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
    }

    public void fetchAllPolls(final FinishFetchingDataCallback finishFetchingDataCallback) {
        db.collection("POLL").orderBy("START_TIME", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            finishFetchingDataCallback.onFailed("No polls available", "POLL");
                            return;
                        } else {
                            List<DocumentSnapshot> documentsList = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot documentSnapshot : documentsList){
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
                                        (String) documentSnapshot.get("STATUS"),
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
    }

    public void fetchActivePolls(final FinishFetchingDataCallback finishFetchingDataCallback, String topic) {
        db.collection("POLL")
                .whereEqualTo("TOPIC", topic)
                .whereLessThanOrEqualTo("START_TIME",currentDate)
                .orderBy("START_TIME", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            finishFetchingDataCallback.onFailed("No polls available", "POLL");
                            return;
                        } else {
                            List<DocumentSnapshot> documentsList = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot documentSnapshot : documentsList){
                                Timestamp start = (Timestamp) documentSnapshot.get("START_TIME");
                                Timestamp close = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                                Timestamp declare = (Timestamp) documentSnapshot.get("DECLARE_TIME");
                                if((close==null) || (close.toDate().after(currentDate))) {
                                    Date start_ = start.toDate();
                                    Date close_ = (close == null) ? null : close.toDate();
                                    Date declare_ = (declare == null) ? null : declare.toDate();
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
                                            (long) documentSnapshot.get("CORRECT_OPTION"),
                                            (long) documentSnapshot.get("REWARD_AMOUNT")
                                    ));
                                }
                            }
                            finishFetchingDataCallback.onFinish(polls,"POLL");
                        }
                    }
                });
    }

    public void fetchClosedPolls(final FinishFetchingDataCallback finishFetchingDataCallback, String topic) {
        db.collection("POLL")
                .whereEqualTo("TOPIC", topic)
                .whereLessThanOrEqualTo("CLOSE_TIME",currentDate)
                .orderBy("CLOSE_TIME", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            finishFetchingDataCallback.onFailed("No polls available", "POLL");
                            return;
                        } else {
                            List<DocumentSnapshot> documentsList = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot documentSnapshot : documentsList){
                                Timestamp start = (Timestamp) documentSnapshot.get("START_TIME");
                                Timestamp close = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                                Timestamp declare = (Timestamp) documentSnapshot.get("DECLARE_TIME");
                                if((declare == null) || (declare.toDate().after(currentDate))) {
                                    Date start_ = start.toDate();
                                    Date close_ = close.toDate();
                                    Date declare_ = (declare == null) ? null : declare.toDate();
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
                                            (long) documentSnapshot.get("CORRECT_OPTION"),
                                            (long) documentSnapshot.get("REWARD_AMOUNT")
                                    ));
                                }
                            }
                            finishFetchingDataCallback.onFinish(polls,"POLL");
                        }
                    }
                });
    }

    public void fetchDeclaredPolls(final FinishFetchingDataCallback finishFetchingDataCallback, String topic) {
        Date currentDate = Calendar.getInstance().getTime();
        Date minDate = new Date(currentDate.getTime() - 864000000);
        System.out.println("minDate : "+minDate);
        db.collection("POLL")
                .whereEqualTo("TOPIC", topic)
                .whereLessThanOrEqualTo("DECLARE_TIME",currentDate)
                .whereGreaterThanOrEqualTo("DECLARE_TIME",minDate)
                .orderBy("DECLARE_TIME", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            finishFetchingDataCallback.onFailed("No polls available", "POLL");
                            return;
                        } else {
                            List<DocumentSnapshot> documentsList = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot documentSnapshot : documentsList){
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
                            finishFetchingDataCallback.onFinish(polls,"POLL");
                        }
                    }
                });
    }
}
