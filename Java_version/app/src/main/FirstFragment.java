package com.termux.wayland;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.termux.wayland.databinding.FragmentFirstBinding;
import com.termux.wayland.launcher.ScriptManager;

import java.io.File;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── Initialiser les scripts au premier lancement ───────────────────────
        binding.tvScriptsStatus.setText(R.string.launcher_loading);
        ScriptManager.initScripts(requireContext(), new ScriptManager.OnScriptsReadyListener() {
            @Override
            public void onReady(File scriptsDir) {
                if (getView() == null) return;
                binding.tvScriptsStatus.setText(R.string.scripts_ready);
            }
            @Override
            public void onError(Exception e) {
                if (getView() == null) return;
                binding.tvScriptsStatus.setText("Scripts error: " + e.getMessage());
            }
        });

        // ── Navigation vers le launcher ───────────────────────────────────────
        binding.buttonFirst.setOnClickListener(v ->
            NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );

        // ── Navigation vers Manage Packages (même destination) ────────────────
        binding.buttonManage.setOnClickListener(v ->
            NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );

        // ── Lancer setup-x11.sh via Termux ───────────────────────────────────
        binding.btnRunSetup.setOnClickListener(v -> {
            try {
                Intent intent = ScriptManager.buildRunScriptIntent(
                    requireContext(), "setup-x11.sh", null
                );
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(),
                    "Termux not found. Install Termux first.", Toast.LENGTH_LONG).show();
            }
        });

        // ── Lancer install-gui-apps.sh via Termux ─────────────────────────────
        binding.btnRunInstall.setOnClickListener(v -> {
            try {
                Intent intent = ScriptManager.buildRunScriptIntent(
                    requireContext(), "install-gui-apps.sh", null
                );
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(),
                    "Termux not found. Install Termux first.", Toast.LENGTH_LONG).show();
            }
        });

        // ── Ouvrir le dossier scripts dans un explorateur de fichiers ─────────
        binding.btnOpenScripts.setOnClickListener(v -> {
            try {
                Intent intent = ScriptManager.buildOpenScriptIntent(
                    requireContext(), "setup-x11.sh"
                );
                // On ouvre le dossier plutôt qu'un fichier précis
                intent.setDataAndType(
                    android.net.Uri.fromFile(ScriptManager.getSystemDir(requireContext())),
                    "resource/folder"
                );
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Open scripts folder"));
            } catch (Exception e) {
                Toast.makeText(requireContext(),
                    "No file manager found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
