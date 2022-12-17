package com.example.g1_final_project.fragments;

import static android.view.LayoutInflater.from;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.g1_final_project.R;
import com.example.g1_final_project.databinding.FragmentHistoryBinding;
import com.example.g1_final_project.models.HistoryItemModel;
import com.example.g1_final_project.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Constants.databaseReference().child(Constants.auth().getUid())
                .child(Constants.HISTORY)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && isAdded()) {

                            historyArrayList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                historyArrayList.add(dataSnapshot.getValue(HistoryItemModel.class));
                            }
                            initRecyclerView();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        return root;
    }

    private ArrayList<HistoryItemModel> historyArrayList = new ArrayList<>();

    private RecyclerView historyRecy;
    private RecyclerViewAdapterMessages adapter;

    private void initRecyclerView() {

        historyRecy = binding.historyRecy;
        adapter = new RecyclerViewAdapterMessages();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
        linearLayoutManager.setReverseLayout(true);
        historyRecy.setLayoutManager(linearLayoutManager);
        historyRecy.setHasFixedSize(true);
        historyRecy.setNestedScrollingEnabled(false);

        historyRecy.setAdapter(adapter);

    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = from(parent.getContext()).inflate(R.layout.history_item, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position) {
            HistoryItemModel model = historyArrayList.get(position);

            holder.title.setText(model.title);
            holder.time.setText("Time: " + model.time);
            holder.distance.setText("Distance: " + model.distance);
        }

        @Override
        public int getItemCount() {
            if (historyArrayList == null)
                return 0;
            return historyArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            TextView title, time, distance;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.title_history);
                time = v.findViewById(R.id.time);
                distance = v.findViewById(R.id.distance);

            }
        }

    }

}