package com.termux.wayland;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.termux.wayland.data.AppRepository;
import com.termux.wayland.data.LinuxApp;
import com.termux.wayland.databinding.FragmentSecondBinding;
import com.termux.wayland.launcher.ScriptManager;
import com.termux.wayland.ui.AppAdapter;

import java.util.List;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private AppAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── RecyclerView ──────────────────────────────────────────────────────
        adapter = new AppAdapter(this::onLaunchApp);
        binding.recyclerApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerApps.setAdapter(adapter);

        // ── Recherche texte ───────────────────────────────────────────────────
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                adapter.filterByQuery(s.toString());
            }
        });

        // ── Filtres catégories ────────────────────────────────────────────────
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip chip = group.findViewById(checkedIds.get(0));
            if (chip == null) return;
            String cat = chip.getText().toString().toLowerCase();
            adapter.filterByCategory(cat.equals("all") ? "all" : cat);
        });

        // ── Navigation retour ─────────────────────────────────────────────────
        binding.buttonSecond.setOnClickListener(v ->
            NavHostFragment.findNavController(SecondFragment.this)
                .navigate(R.id.action_SecondFragment_to_FirstFragment)
        );

        // ── Charger les apps ──────────────────────────────────────────────────
        loadApps();
    }

    private void loadApps() {
        showStatus(getString(R.string.launcher_loading));

        AppRepository.getInstance().getInstalledApps(
            requireContext(),
            new AppRepository.OnAppsLoadedListener() {
                @Override
                public void onAppsLoaded(List<LinuxApp> apps) {
                    if (getView() == null) return;
                    adapter.setApps(apps);
                    if (apps.isEmpty()) {
                        showStatus(getString(R.string.launcher_empty));
                    } else {
                        hideStatus();
                    }
                }
                @Override
                public void onError(Exception e) {
                    if (getView() == null) return;
                    showStatus(getString(R.string.launcher_error));
                }
            }
        );
    }

    // ── Lancement d'une app via Termux RUN_COMMAND ────────────────────────────

    private void onLaunchApp(LinuxApp app) {
        try {
            // Construire la commande : charge l'env X11 puis lance l'app
            String command = "source "
                + ScriptManager.getSystemDir(requireContext()).getAbsolutePath()
                + "/.x11_env 2>/dev/null; "
                + app.getExec();

            Intent intent = new Intent();
            intent.setClassName("com.termux", "com.termux.app.RunCommandService");
            intent.setAction("com.termux.RUN_COMMAND");
            intent.putExtra("com.termux.RUN_COMMAND_PATH",
                "/data/data/com.termux/files/usr/bin/bash");
            intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS",
                new String[]{"-c", command});
            intent.putExtra("com.termux.RUN_COMMAND_WORKDIR",
                "/data/data/com.termux/files/home");
            intent.putExtra("com.termux.RUN_COMMAND_TERMINAL", false);

            requireContext().startService(intent);
            Toast.makeText(requireContext(),
                "Launching " + app.getName() + "…", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(requireContext(),
                "Termux not found. Install Termux first.", Toast.LENGTH_LONG).show();
        }
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────

    private void showStatus(String message) {
        binding.tvStatus.setText(message);
        binding.tvStatus.setVisibility(View.VISIBLE);
        binding.recyclerApps.setVisibility(View.GONE);
    }

    private void hideStatus() {
        binding.tvStatus.setVisibility(View.GONE);
        binding.recyclerApps.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
