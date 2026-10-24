package com.example.hyperos2launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout root, dock, grid;
    private EditText search;
    private boolean drawerOpen = false;
    private final int textColor = Color.WHITE;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        showHome();
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private TextView label(String text, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text); v.setTextSize(size); v.setTextColor(textColor);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private void showHome() {
        drawerOpen = false;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 42, 22, 18);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(35, 62, 112), Color.rgb(93, 92, 160), Color.rgb(27, 31, 61)}));
        setContentView(root);

        TextView date = label(new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(new Date()), 14, false);
        root.addView(date);
        TextView clock = label(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()), 48, false);
        clock.setPadding(0, 8, 0, 0);
        root.addView(clock);
        TextView subtitle = label("HyperOS 2  •  Redmi A5 Edition", 13, false);
        subtitle.setAlpha(.85f);
        root.addView(subtitle);

        Space spacer = new Space(this);
        root.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1));

        LinearLayout shortcuts = new LinearLayout(this);
        shortcuts.setGravity(Gravity.CENTER);
        shortcuts.setOrientation(LinearLayout.HORIZONTAL);
        addShortcut(shortcuts, "Teléfono", "☎", "com.google.android.dialer");
        addShortcut(shortcuts, "Mensajes", "✉", "com.google.android.apps.messaging");
        addShortcut(shortcuts, "Cámara", "◉", "com.android.camera2");
        addShortcut(shortcuts, "Ajustes", "⚙", "com.android.settings");
        root.addView(shortcuts);

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(0xCCFFFFFF);
        search.setHint("⌕  Buscar aplicaciones");
        search.setPadding(18, 4, 18, 4);
        search.setBackground(shape(0x44FFFFFF, 48));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, 50);
        sp.topMargin = 22; sp.bottomMargin = 12;
        root.addView(search, sp);
        search.setOnClickListener(v -> showDrawer(""));
        search.setOnEditorActionListener((v, actionId, event) -> { showDrawer(search.getText().toString()); return true; });

        TextView all = label("Desliza o toca aquí para ver todas las aplicaciones  ↑", 12, false);
        all.setGravity(Gravity.CENTER);
        all.setPadding(0, 6, 0, 12);
        root.addView(all);
        all.setOnClickListener(v -> showDrawer(""));
        root.setOnTouchListener(new View.OnTouchListener() {
            float startY;
            public boolean onTouch(View v, android.view.MotionEvent e) {
                if (e.getAction() == 0) startY = e.getY();
                if (e.getAction() == 1 && startY - e.getY() > 80) { showDrawer(""); return true; }
                return true;
            }
        });
    }

    private void addShortcut(LinearLayout parent, String name, String glyph, String pkg) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        TextView icon = label(glyph, 28, false);
        icon.setGravity(Gravity.CENTER);
        icon.setTextColor(Color.rgb(36, 47, 80));
        icon.setBackground(shape(Color.WHITE, 22));
        item.addView(icon, new LinearLayout.LayoutParams(58, 58));
        TextView title = label(name, 11, false);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 6, 0, 0);
        item.addView(title);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
        parent.addView(item, lp);
        item.setOnClickListener(v -> launchPackage(pkg));
    }

    private void showDrawer(String filter) {
        drawerOpen = true;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 28, 18, 12);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(28, 39, 73), Color.rgb(66, 66, 110)}));
        setContentView(root);

        TextView heading = label("Aplicaciones", 26, true);
        root.addView(heading);
        EditText box = new EditText(this);
        box.setSingleLine(true);
        box.setTextColor(Color.WHITE);
        box.setHintTextColor(0xCCFFFFFF);
        box.setHint("Buscar");
        box.setBackground(shape(0x33FFFFFF, 40));
        box.setPadding(16, 2, 16, 2);
        root.addView(box, new LinearLayout.LayoutParams(-1, 48));
        ScrollView scroll = new ScrollView(this);
        grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(grid);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, 0, 1);
        slp.topMargin = 12;
        root.addView(scroll, slp);
        box.setText(filter);
        populateApps(filter);
        box.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) { populateApps(s.toString()); }
            public void afterTextChanged(android.text.Editable e) {}
        });
        TextView back = label("← Volver al inicio", 14, true);
        back.setGravity(Gravity.CENTER);
        back.setPadding(0, 12, 0, 8);
        root.addView(back);
        back.setOnClickListener(v -> showHome());
    }

    private void populateApps(String filter) {
        if (grid == null) return;
        grid.removeAllViews();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = new ArrayList<>(pm.getInstalledApplications(PackageManager.GET_META_DATA));
        apps.sort((a,b) -> pm.getApplicationLabel(a).toString().compareToIgnoreCase(pm.getApplicationLabel(b).toString()));
        LinearLayout row = null;
        int col = 0;
        for (ApplicationInfo app : apps) {
            String name = pm.getApplicationLabel(app).toString();
            if (name.equalsIgnoreCase("HyperOS 2 Launcher")) continue;
            if (filter != null && !filter.trim().isEmpty() && !name.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))) continue;
            if (col == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.TOP);
                grid.addView(row, new LinearLayout.LayoutParams(-1, -2));
            }
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(4, 12, 4, 12);
            try {
                ImageView image = new ImageView(this);
                image.setImageDrawable(pm.getApplicationIcon(app));
                row.addView(cell, new LinearLayout.LayoutParams(0, -2, 1));
                cell.addView(image, new LinearLayout.LayoutParams(52, 52));
                TextView label = label(name, 11, false);
                label.setGravity(Gravity.CENTER);
                label.setMaxLines(2);
                cell.addView(label, new LinearLayout.LayoutParams(-1, -2));
                cell.setOnClickListener(v -> {
                    Intent intent = pm.getLaunchIntentForPackage(app.packageName);
                    if (intent != null) startActivity(intent);
                });
            } catch (Exception ignored) {}
            col++;
            if (col == 4) col = 0;
        }
    }

    private void launchPackage(String pkg) {
        PackageManager pm = getPackageManager();
        Intent i = pm.getLaunchIntentForPackage(pkg);
        if (i != null) startActivity(i);
        else new AlertDialog.Builder(this).setMessage("Esta aplicación no está disponible en este dispositivo.")
                .setPositiveButton("Aceptar", null).show();
    }

    @Override public void onBackPressed() {
        if (drawerOpen) showHome(); else super.onBackPressed();
    }
}
