package com.github.egubot.shared.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileIndexer {
    private final Map<String, List<String>> fileIndex = new HashMap<>();

    public FileIndexer(String folder) throws IOException {
        indexDirectory(Paths.get(folder));
    }

    private void indexDirectory(Path startPath) throws IOException {
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                String parentDirName = file.getParent().getFileName().toString();

                fileIndex.computeIfAbsent(fileName, k -> new ArrayList<>()).add(parentDirName);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public List<String> getDirectoriesContainingFile(String fileName) {
        return fileIndex.getOrDefault(fileName, new ArrayList<>());
    }

    public static void main(String[] args) throws IOException {
        FileIndexer indexer = new FileIndexer("Storage");

        // Example usage
        List<String> directories = indexer.getDirectoriesContainingFile("Timers.txt");
        System.out.println("Directories containing Timers.txt: " + directories);
    }
}
