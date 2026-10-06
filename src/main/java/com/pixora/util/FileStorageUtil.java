package com.pixora.util;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class FileStorageUtil {
    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> EVIDENCE_EXT = Set.of("jpg", "jpeg", "png", "webp", "pdf");

    private FileStorageUtil() {}

    public static String saveImage(Part part, String folder) throws IOException {
        return save(part, folder, IMAGE_EXT, AppConfig.getInt("app.maxPhotoSizeMb", 8));
    }

    public static String saveEvidence(Part part, String folder) throws IOException {
        return save(part, folder, EVIDENCE_EXT, AppConfig.getInt("app.maxEvidenceSizeMb", 5));
    }

    private static String save(Part part, String folder, Set<String> allowed, int maxMb) throws IOException {
        if (part == null || part.getSize() == 0) return null;
        if (part.getSize() > maxMb * 1024L * 1024L) throw new IOException("File exceeds " + maxMb + " MB limit.");

        String submitted = part.getSubmittedFileName();
        String ext = "";
        int dot = submitted == null ? -1 : submitted.lastIndexOf('.');
        if (dot >= 0) ext = submitted.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!allowed.contains(ext)) throw new IOException("Unsupported file type.");

        Path root = AppConfig.uploadRoot();
        Path dir = root.resolve(folder).normalize();
        if (!dir.startsWith(root)) throw new IOException("Unsafe upload path.");
        Files.createDirectories(dir);

        String safeName = UUID.randomUUID() + "." + ext;
        Path target = dir.resolve(safeName);
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return folder + "/" + safeName;
    }

    public static Path resolve(String relative) throws IOException {
        if (relative == null) throw new IOException("Missing file.");
        Path root = AppConfig.uploadRoot();
        Path path = root.resolve(relative).normalize();
        if (!path.startsWith(root)) throw new IOException("Unsafe path.");
        return path;
    }

    public static void deleteQuietly(String relative) {
        try {
            if (relative != null) Files.deleteIfExists(resolve(relative));
        } catch (IOException ignored) {}
    }
}
