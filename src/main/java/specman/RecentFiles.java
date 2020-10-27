package specman;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class RecentFiles {
    private static final String RECENT_FILES_PREF = "recent.files";
    private static final String FILE_SEPARATOR = ";";
    private static final int MAX_FILES = 5;

    private final JMenu menu;
    private final EditorI editor;
    List<File> recentFiles;

    RecentFiles(EditorI editor) {
        this.editor = editor;
        this.menu = new JMenu("Zuletzt geladen");
        this.recentFiles = readLastFilesFromPreferences();
        populateMenuFromPreferences();
    }

    private void populateMenuFromPreferences() {
        menu.removeAll();
        for (File lastFile: recentFiles) {
            JMenuItem item = new JMenuItem(lastFile.getName());
            item.setToolTipText(lastFile.getAbsolutePath());
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editor.diagrammLaden(lastFile);
                }
            });
            menu.add(item);
        }
    }

    private List<File> readLastFilesFromPreferences() {
        List<File> result = new ArrayList<>();
        Preferences prefs = Preferences.userNodeForPackage(Specman.class);
        String lastFileNames = prefs.get(RECENT_FILES_PREF, null);
        if (lastFileNames != null) {
            for (String lastFilename: lastFileNames.split(FILE_SEPARATOR)) {
                File lastFile = new File(lastFilename);
                if (lastFile.exists()) {
                    result.add(lastFile);
                }
            }
        }
        return result;
    }

    void add(File newest) {
        recentFiles.remove(newest);
        recentFiles.add(0, newest);
        if (recentFiles.size() > MAX_FILES) {
            recentFiles.remove(MAX_FILES);
        }
        writeRecentFilesToPreferences(recentFiles);
        populateMenuFromPreferences();
    }

    private void writeRecentFilesToPreferences(List<File> recentFiles) {
        Preferences prefs = Preferences.userNodeForPackage(Specman.class);
        String prefValue = recentFiles
                .stream()
                .map(file -> file.getAbsolutePath())
                .collect(Collectors.joining(FILE_SEPARATOR));
        prefs.put(RECENT_FILES_PREF, prefValue);
    }

    JMenu menu() {
        return menu;
    }
}
