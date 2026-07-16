package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipFile;

@Service
public class LocalStorageService {

    private static final Map<String, String> MIME_BY_EXTENSION = Map.ofEntries(
            Map.entry("jpg", "image/jpeg"), Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"), Map.entry("webp", "image/webp"),
            Map.entry("pdf", "application/pdf"), Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    );
    private static final Set<String> ALLOWED_MIME_TYPES = Set.copyOf(MIME_BY_EXTENSION.values());

    private final Path root;
    private final long maxFileSize;

    @Autowired
    public LocalStorageService(
            @Value("${xiangyun.storage.root:/data/xiangyun/uploads}") String root,
            @Value("${xiangyun.storage.max-file-size-bytes:10485760}") long maxFileSize) {
        this(Path.of(root), maxFileSize);
    }

    LocalStorageService(Path root, long maxFileSize) {
        this.root = root.toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
        try {
            Files.createDirectories(this.root);
        } catch (IOException ex) {
            throw new IllegalStateException("无法初始化文件存储目录", ex);
        }
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40021, "请选择需要上传的文件");
        }
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(41300, "单个文件不能超过 10MB");
        }
        String originalName = safeOriginalName(file.getOriginalFilename());
        String extension = extension(originalName);
        String expectedMime = MIME_BY_EXTENSION.get(extension);
        if (expectedMime == null) {
            throw new BusinessException(40022, "仅支持 JPG、PNG、WEBP、PDF、Word 和 Excel 文件");
        }
        String claimedMime = normalizeMime(file.getContentType());
        String contentType = "application/octet-stream".equals(claimedMime) ? expectedMime : claimedMime;
        if ("application/zip".equals(contentType) && ("docx".equals(extension) || "xlsx".equals(extension))) {
            contentType = expectedMime;
        }
        if (!ALLOWED_MIME_TYPES.contains(contentType) || !compatible(extension, contentType)) {
            throw new BusinessException(40023, "文件扩展名与内容类型不匹配");
        }

        String partition = LocalDate.now().toString().replace("-", "/");
        String storageKey = partition + "/" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path destination = resolve(storageKey);
        Path temporary = null;
        try {
            Files.createDirectories(destination.getParent());
            temporary = Files.createTempFile(root, ".upload-", ".tmp");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream source = new BufferedInputStream(file.getInputStream());
                 DigestInputStream input = new DigestInputStream(source, digest)) {
                Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
            }
            long actualSize = Files.size(temporary);
            if (actualSize <= 0 || actualSize > maxFileSize) {
                throw new BusinessException(41300, "文件为空或超过 10MB 限制");
            }
            validateSignature(temporary, contentType);
            moveAtomically(temporary, destination);
            temporary = null;
            return new StoredFile(storageKey, originalName, contentType, actualSize,
                    HexFormat.of().formatHex(digest.digest()));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(50021, "文件保存失败，请稍后重试");
        } finally {
            deleteQuietly(temporary);
        }
    }

    public Resource open(String storageKey) {
        try {
            Path path = resolve(storageKey);
            if (!Files.isRegularFile(path)) {
                throw new BusinessException(40421, "材料文件不存在");
            }
            return new UrlResource(path.toUri());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(40421, "材料文件不存在");
        }
    }

    public void deletePhysical(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return;
        deleteQuietly(resolve(storageKey));
    }

    Path root() {
        return root;
    }

    private Path resolve(String storageKey) {
        Path resolved = root.resolve(storageKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new BusinessException(40024, "非法文件路径");
        }
        return resolved;
    }

    private String safeOriginalName(String originalName) {
        String value = originalName == null ? "unnamed" : originalName.replace('\\', '/');
        value = value.substring(value.lastIndexOf('/') + 1).trim();
        if (value.isBlank() || value.length() > 255 || value.contains("..")) {
            throw new BusinessException(40024, "文件名不合法");
        }
        return value;
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeMime(String contentType) {
        if (contentType == null || contentType.isBlank()) return "application/octet-stream";
        int separator = contentType.indexOf(';');
        return (separator < 0 ? contentType : contentType.substring(0, separator)).trim().toLowerCase(Locale.ROOT);
    }

    private boolean compatible(String extension, String contentType) {
        String expected = MIME_BY_EXTENSION.get(extension);
        if (expected.equals(contentType)) return true;
        return ("docx".equals(extension) || "xlsx".equals(extension)) && "application/zip".equals(contentType);
    }

    private void validateSignature(Path file, String contentType) throws IOException {
        byte[] header = new byte[12];
        int count;
        try (InputStream input = Files.newInputStream(file)) {
            count = input.read(header);
        }
        boolean valid = switch (contentType) {
            case "image/jpeg" -> count >= 3 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff;
            case "image/png" -> count >= 8 && header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47;
            case "image/webp" -> count >= 12 && new String(header, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
                    && new String(header, 8, 4, StandardCharsets.US_ASCII).equals("WEBP");
            case "application/pdf" -> count >= 5 && new String(header, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-");
            case "application/msword", "application/vnd.ms-excel" -> oleSignature(header, count);
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    zipSignature(header, count) && containsOpenXmlPart(file, "word/document.xml");
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
                    zipSignature(header, count) && containsOpenXmlPart(file, "xl/workbook.xml");
            default -> true;
        };
        if (!valid) {
            throw new BusinessException(40025, "文件内容校验失败");
        }
    }

    private boolean oleSignature(byte[] header, int count) {
        int[] expected = { 0xd0, 0xcf, 0x11, 0xe0, 0xa1, 0xb1, 0x1a, 0xe1 };
        if (count < expected.length) return false;
        for (int index = 0; index < expected.length; index++) {
            if ((header[index] & 0xff) != expected[index]) return false;
        }
        return true;
    }

    private boolean zipSignature(byte[] header, int count) {
        return count >= 4 && header[0] == 0x50 && header[1] == 0x4b
                && header[2] == 0x03 && header[3] == 0x04;
    }

    private boolean containsOpenXmlPart(Path file, String requiredPart) {
        try (ZipFile archive = new ZipFile(file.toFile())) {
            return archive.getEntry("[Content_Types].xml") != null && archive.getEntry(requiredPart) != null;
        } catch (IOException ex) {
            return false;
        }
    }

    private void moveAtomically(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 清理失败留给部署侧定时孤儿文件扫描处理。
        }
    }
}
