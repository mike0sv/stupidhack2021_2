package com.example.myapplication;

import android.app.usage.StorageStatsManager;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
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

    public float getTotalBytes() {
        long totalBytes = 1;
        try {
            totalBytes = ssm.getTotalBytes(StorageManager.UUID_DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalBytes;
    }

    public float getFreeBytes() {
        long freeBytes = 0;
        try {
            freeBytes = ssm.getFreeBytes(StorageManager.UUID_DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return freeBytes;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        float freeBytes = getFreeBytes();
        float totalBytes = getTotalBytes();

        String msg = String.format("Hello %s %s\n%s %s",
                freeBytes,
                freeBytes / BYTES_IN_GB,
                totalBytes,
                totalBytes / BYTES_IN_GB);
        binding.textView.setText(msg);
        WebView webviewContent = binding.webviewContent;
        webviewContent.getSettings().setJavaScriptEnabled(true);
        webviewContent.getSettings().setLoadWithOverviewMode(true);
        webviewContent.addJavascriptInterface(new Object()
        {
            @JavascriptInterface
            public float totalBytes()
            {
                return getTotalBytes();
            }
            @JavascriptInterface
            public float freeBytes()
            {
                return getFreeBytes();
            }
        }, "Android");
//        webviewContent.loadUrl("https://ahfarmer.github.io/calculator/");
        binding.webviewContent.loadUrl("file:///android_asset/test.html");
        binding.buttonReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webviewContent.reload();
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        binding.buttonStatic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webviewContent.loadUrl("file:///android_asset/test.html");
            }
        });

        binding.buttonServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webviewContent.loadUrl(binding.inputUrlEdit.getText().toString());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}