package com.manit.hostel.assist.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.manit.hostel.assist.R;
import com.manit.hostel.assist.data.Entries;
import com.manit.hostel.assist.databinding.StudentViewBinding;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

@SuppressLint("NotifyDataSetChanged")
public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.EntriesViewHolder> {
    public static final int ALL_FILTER = 1;
    public static final int EXIT_ONLY_FILTER = 2;
    public static final int ENTERED_FILTER = 3;

    public ArrayList<Entries> getOriginalEntriesList() {
        return originalEntriesList;
    }

    private ArrayList<Entries> originalEntriesList;
    private ArrayList<Entries> filteredEntriesList;
    private String todaysDateStringInYYYY_MM_DD = "";

    // Constructor to accept the list
    public EntriesAdapter(ArrayList<Entries> entriesList) {
        this.originalEntriesList = new ArrayList<>(entriesList);
        this.filteredEntriesList = new ArrayList<>(entriesList);
        this.todaysDateStringInYYYY_MM_DD = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }


    @NonNull
    @Override
    public EntriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Using ViewBinding for item_entry.xml
        StudentViewBinding binding = StudentViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EntriesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EntriesViewHolder holder, int position) {
        // Get current entry from the filtered list
        Entries currentEntry = filteredEntriesList.get(position);
        holder.binding.index.setText(String.valueOf(filteredEntriesList.get(position).getEntryNo()));
        // Bind data to the views using the binding object
//        holder.binding.entryNo.setText("Entry No. - " + currentEntry.getEntryNo());
        holder.binding.name.setText("Name: " + currentEntry.getName());
        holder.binding.roomNo.setText("Room No: " + currentEntry.getRoomNo());
        holder.binding.scholarNo.setText("Scholar No. - " + currentEntry.getScholarNo());

        holder.binding.exitTime.setText(currentEntry.getExitTime().replace(todaysDateStringInYYYY_MM_DD, ""));
        holder.binding.entryTime.setText(currentEntry.getEntryTime().replace(todaysDateStringInYYYY_MM_DD, ""));
        Glide.with(holder.binding.getRoot().getContext()).load(currentEntry.getPhotoURL()).placeholder(R.drawable.demo_pic1).error(R.drawable.baseline_error_24).into(holder.binding.studentImageview);

    }

    @Override
    public int getItemCount() {
        return filteredEntriesList.size();
    }

    // Method to filter only entries that have an exit time

    public void filterExitOnly() {
        filteredEntriesList = (ArrayList<Entries>) originalEntriesList.stream().filter(entry -> entry.getEntryTime() == null || entry.getEntryTime().isEmpty()).collect(Collectors.toList());
        notifyDataSetChanged();
        Log.d(EntriesAdapter.class.getSimpleName(), "filteredEntriesList : Exit Only Filter");
    }

    public void filterEntered() {
        filteredEntriesList = (ArrayList<Entries>) originalEntriesList.stream().filter(entry -> entry.getEntryTime() != null && !entry.getEntryTime().isEmpty()).collect(Collectors.toList());
        Log.d(EntriesAdapter.class.getSimpleName(), "filteredEntriesList : Entered Filter");
        notifyDataSetChanged();
    }

    // Reset filter to show all entries
    public void resetFilter() {
        filteredEntriesList = new ArrayList<>(originalEntriesList);
        notifyDataSetChanged();
    }

    public void updateEntries(ArrayList<Entries> entriesList) {
        this.originalEntriesList = new ArrayList<>(entriesList);
        this.filteredEntriesList = new ArrayList<>(entriesList);
        notifyDataSetChanged();
    }

    public void filterEntries(int enteredFilter) {
        switch (enteredFilter) {
            case ALL_FILTER:
                resetFilter();
                break;
            case EXIT_ONLY_FILTER:
                filterExitOnly();
                break;
            case ENTERED_FILTER:
                filterEntered();
                break;
        }
    }

    public void filterSearch(String txt) {
        filteredEntriesList = (ArrayList<Entries>) originalEntriesList.stream().filter(entry -> {
            String lowerCase = txt.toLowerCase();
            return entry.getName().toLowerCase().contains(lowerCase) || entry.getRoomNo().contains(lowerCase) || entry.getScholarNo().toLowerCase().contains(lowerCase) || entry.getEntryNo().toLowerCase().contains(lowerCase);
        }).collect(Collectors.toList());
        notifyDataSetChanged();
    }

    // ViewHolder class using ViewBinding
    public static class EntriesViewHolder extends RecyclerView.ViewHolder {
        StudentViewBinding binding;

        public EntriesViewHolder(StudentViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
