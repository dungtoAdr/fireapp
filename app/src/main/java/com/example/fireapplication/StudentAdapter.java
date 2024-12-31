package com.example.fireapplication;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {
    private Context context;
    private List<Student> students;

    public StudentAdapter(Context context, List<Student> students) {
        super(context, R.layout.student_item, students);
        this.context = context;
        this.students = students;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.student_item, parent, false);
        }

        Student student = students.get(position);

        TextView tvId = convertView.findViewById(R.id.tvId);
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvEmail = convertView.findViewById(R.id.tvEmail);
        TextView tvGender = convertView.findViewById(R.id.tvGender);

        tvId.setText(student.getId());
        tvName.setText(student.getName());
        tvEmail.setText(student.getEmail());
        tvGender.setText(student.getGender());

        return convertView;
    }
}
