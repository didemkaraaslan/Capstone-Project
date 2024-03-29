package com.owners.pet.petowners;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.collect.Ordering;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.owners.pet.petowners.adapters.UserAdapter;
import com.owners.pet.petowners.models.ChatUser;
import com.owners.pet.petowners.models.Message;
import com.owners.pet.petowners.models.User;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {
    @BindView(R.id.conversation_list_list_view)
    ListView convListLv;

    public ChatFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, rootView);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (currentUser != null) {
            db.collection(getString(R.string.COLLECTION_USERS))
                    .document(currentUser.getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if (snapshot != null && snapshot.exists()) {
                                final User user = snapshot.toObject(User.class);
                                int i = 0;
                                if (user != null) {
                                    for (String uid : user.getChatWithUidList()) {
                                        if (uid != null) {
                                            final int finalI = i;
                                            db.collection(getString(R.string.COLLECTION_MESSAGES))
                                                    .document(currentUser.getUid())
                                                    .collection(uid)
                                                    .orderBy(getString(R.string.DATE_KEY), Query.Direction.DESCENDING)
                                                    .limit(1)
                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onEvent(@javax.annotation.Nullable QuerySnapshot snap, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                                            if (snap != null) {
                                                                Message m = snap.getDocuments().get(0).toObject(Message.class);
                                                                if (m != null) {
                                                                    String lastMessage = m.getContent();
                                                                    Date lastMessageDate = m.getDate();
                                                                    user.getConversationList().get(finalI).setLastMessage(lastMessage);
                                                                    user.getConversationList().get(finalI).setLastMessageDate(lastMessageDate);
                                                                }

                                                                loadConversations(user.getConversationList());
                                                            }
                                                        }
                                                    });
                                        }
                                        i++;
                                    }
                                }
                            }
                        }
                    });
        }

        return rootView;
    }


    private void loadConversations(ArrayList<ChatUser> conversationList) {
        if (getContext() != null) {
            UserAdapter adapter = new UserAdapter(getContext(), conversationList);
            adapter.notifyDataSetChanged();
            convListLv.setAdapter(adapter);
        }
    }
}
