package com.example.myapplication;

import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentFirstBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.Random;

public class FirstFragment extends Fragment {
    private static final int BYTES_IN_GB = 1000 * 1000 * 1000;
    private static final int INIT_DOGGOS = 5;
    private static final String CHUNK;
    public static final String PREFIX = "kek";
    public static final String PREF_COUNT_KEY = "pref_count";
    public static final int CHUNK_TIMES = 200 * 1000;

    static {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            builder.append("0");
        }
        CHUNK = builder.toString();
    }

    private FragmentFirstBinding binding;
    private StorageStatsManager ssm;
    private Vibrator vibe;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        ssm = getActivity().getSystemService(StorageStatsManager.class);
        vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
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

    private void writeToFile(String filename, String data, int times) throws IOException {
        Context context = getContext();
        FileOutputStream stream = context.openFileOutput(filename, Context.MODE_PRIVATE);
        OutputStreamWriter outputStreamWriter =
                new OutputStreamWriter(stream);
        for (int i = 0; i < times; i++) {
            outputStreamWriter.write(data);
        }
        outputStreamWriter.close();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        float freeBytes = getFreeBytes();
        float totalBytes = getTotalBytes();

        int count = updateCount(0);
        if (count == -1) {
            updateCount(1);
            for (int i = 0; i < INIT_DOGGOS; i++) {
                giveToDoggo();
            }
        }


        String msg = String.format("Hello %s %s\n%s %s",
                freeBytes,
                freeBytes / BYTES_IN_GB,
                totalBytes,
                totalBytes / BYTES_IN_GB);
        binding.textView.setText(msg);
        WebView webviewContent = binding.webviewContent;
        webviewContent.getSettings().setJavaScriptEnabled(true);
        webviewContent.getSettings().setLoadWithOverviewMode(true);
        webviewContent.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public float totalBytes() {
                return getTotalBytes();
            }

            @JavascriptInterface
            public float freeBytes() {
                return getFreeBytes() + claimedBytes();
            }

            @JavascriptInterface
            public float trueFreeBytes() {
                return getFreeBytes();
            }

            @JavascriptInterface
            public void giveToDoggo() {
                FirstFragment.this.giveToDoggo();
            }

            @JavascriptInterface
            public void takeFromDoggo() {
                String[] fileList = getContext().fileList();
                for (String path : fileList) {
                    if (path.startsWith(PREFIX) && (!path.equals(PREFIX) || fileList.length == 1)) {
                        getContext().deleteFile(path);
                        updateCount(-1);
                        vibe.vibrate(VibrationEffect.createOneShot(1200, 100));
                        break;
                    }
                }
            }

            @JavascriptInterface
            public int claimedCount() {
                return updateCount(0);
            }

            @JavascriptInterface
            public String listFiles() {
                return String.join(", ", getContext().fileList());
            }

            @JavascriptInterface
            public float claimedBytes() {
                float claimed = 0;
                for (String path : getContext().fileList()) {
                    File file = new File(getContext().getFilesDir() + "/" + path);
                    Log.i("Info", String.format("%s %s", path, file.exists()));
                    claimed += file.length();
                }
                return claimed;
            }
        }, "Android");
//        webviewContent.loadUrl("https://ahfarmer.github.io/calculator/");
//        binding.webviewContent.loadUrl("file:///android_asset/test.html");
        webviewContent.loadUrl("http://3.95.71.92:8880");
//        webviewContent.loadUrl("http://192.168.1.225:8100");
        binding.buttonReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webviewContent.reload();
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
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

    private int updateCount(int delta) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int count = sharedPref.getInt(PREF_COUNT_KEY, -1);
        if (delta != 0) {
            editor.putInt(PREF_COUNT_KEY, count + delta);
            editor.apply();
        }
        return count + delta;
    }

    private void giveToDoggo() {
        File sample = new File(getContext().getFilesDir() + "/" + PREFIX);
        if (!sample.exists()) {
            try {
                writeToFile(PREFIX, CHUNK, CHUNK_TIMES);
                updateCount(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                String newPath = PREFIX + new Random().nextInt();
                Files.copy(
                        sample.toPath(),
                        getContext().openFileOutput(newPath, Context.MODE_PRIVATE)
                );
                updateCount(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}