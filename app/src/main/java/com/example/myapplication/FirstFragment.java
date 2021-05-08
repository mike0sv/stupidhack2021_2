package com.example.myapplication;

import android.app.usage.StorageStatsManager;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentFirstBinding;

import java.io.IOException;

public class FirstFragment extends Fragment {
    private static final int BYTES_IN_GB = 1000 * 1000 * 1000;
    private FragmentFirstBinding binding;
    private StorageStatsManager ssm;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        ssm = getActivity().getSystemService(StorageStatsManager.class);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long freeBytes = 0;
        long totalBytes = 1;
        try {
            freeBytes = ssm.getFreeBytes(StorageManager.UUID_DEFAULT);
            totalBytes = ssm.getTotalBytes(StorageManager.UUID_DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        binding.textView.setText("Hello " + freeBytes + " " + ((float) freeBytes) / BYTES_IN_GB +
                "\n" + totalBytes + " " + ((float) totalBytes) / BYTES_IN_GB);
        WebView webviewContent = binding.webviewContent;
        webviewContent.getSettings().setJavaScriptEnabled(true);
        webviewContent.getSettings().setLoadWithOverviewMode(true);
        webviewContent.loadUrl("https://ahfarmer.github.io/calculator/");
//        binding.webviewContent.loadUrl("https://google.com");
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}