package com.sameetasadullah.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class showSusApps extends AppCompatActivity {
    List<ApplicationInfo> susApps;
    List<String> namesOfSusApps;
    RecyclerView recyclerView;
    RVAdaptor rvAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sus_apps);

        recyclerView = findViewById(R.id.recyclerView);
        susApps = (List<ApplicationInfo>) getIntent().getSerializableExtra("susApps");
        namesOfSusApps = new ArrayList<>();

        getNamesOfApps();

        rvAdaptor = new RVAdaptor(this, namesOfSusApps, susApps);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(rvAdaptor);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(20));
    }

    private void getNamesOfApps() {
        for (ApplicationInfo app : susApps) {
            StringTokenizer st = new StringTokenizer(app.packageName,".");
            String name = "";
            while (st.hasMoreTokens()) {
                name = st.nextToken();
            }
            namesOfSusApps.add(name);
        }
    }
}