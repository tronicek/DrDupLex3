package edu.tarleton.drduplex.index.compressed.persistent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * The files with the source code.
 *
 * @author Zdenek Tronicek
 */
public class CPFilePaths {

    private final Map<String, Long> map;
    private long nextFileId;

    private CPFilePaths(Map<String, Long> map, long nextFileId) {
        this.map = map;
        this.nextFileId = nextFileId;
    }

    public static void initialize(Storage storage) throws IOException {
        File pathFile = storage.getPathFile();
        try (FileChannel ch = FileChannel.open(pathFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ch.truncate(0);
        }
    }

    public static CPFilePaths load(Storage storage) throws IOException {
        Map<String, Long> map = new HashMap<>();
        long fileId = 0L;
        File pathFile = storage.getPathFile();
        if (!pathFile.exists()) {
            return new CPFilePaths(map, fileId);
        }
        try (DataInputStream in = new DataInputStream(new FileInputStream(pathFile))) {
            try {
                while (true) {
                    String path = in.readUTF();
                    map.put(path, fileId);
                    fileId++;
                }
            } catch (EOFException e) {
                // okay
            }
        }
        return new CPFilePaths(map, fileId);
    }

    public void append(Storage storage, String path) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(storage.getPathFile(), true))) {
            out.writeUTF(path);
        }
    }

    public long toFileId(Storage storage, String path) throws IOException {
        Long fid = map.get(path);
        if (fid != null) {
            return fid;
        }
        append(storage, path);
        map.put(path, nextFileId);
        nextFileId++;
        return nextFileId - 1;
    }

    public Map<Long, String> getInverseMap() {
        Map<Long, String> imap = new HashMap<>();
        for (String fn : map.keySet()) {
            Long fileId = map.get(fn);
            imap.put(fileId, fn);
        }
        return imap;
    }

    public void print(Storage storage) throws IOException {
        long fileId = 0L;
        try (DataInputStream in = new DataInputStream(new FileInputStream(storage.getPathFile()))) {
            try {
                while (true) {
                    String path = in.readUTF();
                    System.out.printf("%d %s%n", fileId, path);
                    fileId++;
                }
            } catch (EOFException e) {
                // okay
            }
        }
    }
}
