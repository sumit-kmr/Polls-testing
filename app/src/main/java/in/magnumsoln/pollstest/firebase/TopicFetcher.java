package in.magnumsoln.pollstest.firebase;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import in.magnumsoln.pollstest.interfaces.FinishFetchingDataCallback;
import in.magnumsoln.pollstest.model.Topic;

public class TopicFetcher {
    private FirebaseFirestore db;
    private List<Topic> topics;

    public TopicFetcher()
    {
        db = FirebaseFirestore.getInstance();
        topics = new ArrayList<>();
    }

    public void fetchTopics(final FinishFetchingDataCallback finishFetchingDataCallback)
    {
        db.collection("TOPIC").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.isEmpty())
                        {
                            // empty collection
                            finishFetchingDataCallback.onFailed("No topics","TOPIC");
                        }else
                        {
                            List<DocumentSnapshot> documentsList = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot doc : documentsList){
                                topics.add(new Topic(
                                        (String) doc.getId(),
                                        (String) doc.get("IMAGE_URL"),
                                        (String) doc.get("SHARE_URL")
                                ));
                            }
                            Collections.sort(topics);
                            finishFetchingDataCallback.onFinish(topics,"TOPIC");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        finishFetchingDataCallback.onFailed(null,"TOPIC");
                    }
                });
    }
}
