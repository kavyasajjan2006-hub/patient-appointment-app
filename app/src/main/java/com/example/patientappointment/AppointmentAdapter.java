package com.example.patientappointment;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private final List<Appointment> list;

    public AppointmentAdapter(List<Appointment> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = list.get(position);

        holder.tDoc.setText(String.format("%s (%s)", appointment.getDoctor(), appointment.getSpecialization()));
        holder.tDetails.setText(String.format("%s at %s", appointment.getDate(), appointment.getTime()));

        // Handle the dedicated cancel button click
        holder.btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Cancel Appointment")
                    .setMessage("Delete your appointment with " + appointment.getDoctor() + "?")
                    .setPositiveButton("Yes, Cancel", (dialog, which) -> {

                        FirebaseDatabase.getInstance().getReference("Appointments")
                                .child(appointment.getId())
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(v.getContext(), "Appointment Removed", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tDoc, tDetails;
        ImageButton btnCancel;

        ViewHolder(View v) {
            super(v);
            tDoc = v.findViewById(R.id.tvDoctorItem);
            tDetails = v.findViewById(R.id.tvDateItem);
            btnCancel = v.findViewById(R.id.btnCancel);
        }
    }
}