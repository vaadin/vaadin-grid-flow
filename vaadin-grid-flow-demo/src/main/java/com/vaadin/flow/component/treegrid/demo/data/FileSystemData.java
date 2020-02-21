package com.vaadin.flow.component.treegrid.demo.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.treegrid.demo.entity.FileSystemItem;

public class FileSystemData {

    private static final List<FileSystemItem> FILE_LIST = createFileList();

    private static List<FileSystemItem> createFileList() {
        List<FileSystemItem> fileList = new ArrayList<>();

        fileList.add(
                new FileSystemItem(VaadinIcon.FOLDER_O, "Documents", null));
        fileList.add(new FileSystemItem(VaadinIcon.FILE_PICTURE, "Cat Picture",
                fileList.get(0)));
        fileList.add(new FileSystemItem(VaadinIcon.FILE_MOVIE,
                "My Favorite Movie", fileList.get(0)));
        fileList.add(new FileSystemItem(VaadinIcon.FILE_SOUND,
                "My Favorite Song", fileList.get(0)));
        fileList.add(new FileSystemItem(VaadinIcon.FOLDER_O, "WORK", null));
        fileList.add(new FileSystemItem(VaadinIcon.FILE_PRESENTATION,
                "Meeting Presentation", fileList.get(4)));
        fileList.add(new FileSystemItem(VaadinIcon.FILE_TABLE, "Spreadsheet",
                fileList.get(4)));
        fileList.add(new FileSystemItem(VaadinIcon.FILE_ZIP, "Backup", null));

        return fileList;
    }

    public List<FileSystemItem> getFileList() {
        return FILE_LIST;
    }

    public List<FileSystemItem> getRootFiles() {
        return FILE_LIST.stream().filter(file -> file.getParent() == null)
                .collect(Collectors.toList());
    }

    public List<FileSystemItem> getChildFiles(FileSystemItem parent) {
        return FILE_LIST.stream()
                .filter(file -> Objects.equals(file.getParent(), parent))
                .collect(Collectors.toList());
    }
}
