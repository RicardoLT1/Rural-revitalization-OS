package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    @TempDir
    Path root;

    @Test
    void storesWithRandomKeyOutsideApplicationAssets() throws Exception {
        LocalStorageService service = new LocalStorageService(root, 1024 * 1024);
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3, 4};

        StoredFile stored = service.store(new MockMultipartFile(
                "file", "现场照片.png", "image/png", png));

        assertThat(stored.storageKey()).doesNotContain("现场照片").endsWith(".png");
        assertThat(stored.sha256()).hasSize(64);
        assertThat(Files.isRegularFile(root.resolve(stored.storageKey()))).isTrue();
    }

    @Test
    void rejectsDisguisedOrUnsupportedFiles() {
        LocalStorageService service = new LocalStorageService(root, 1024 * 1024);

        assertThatThrownBy(() -> service.store(new MockMultipartFile(
                "file", "malware.exe", "application/octet-stream", new byte[]{1, 2, 3})))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.store(new MockMultipartFile(
                "file", "fake.png", "image/png", new byte[]{1, 2, 3, 4, 5, 6, 7, 8})))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("内容校验失败");
    }

    @Test
    void rejectsOfficeFilesWithoutExpectedContainerSignature() {
        LocalStorageService service = new LocalStorageService(root, 1024 * 1024);

        assertThatThrownBy(() -> service.store(new MockMultipartFile(
                "file", "proof.doc", "application/msword", "plain text".getBytes())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("内容校验失败");
        assertThatThrownBy(() -> service.store(new MockMultipartFile(
                "file", "proof.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[]{0x50, 0x4b, 0x03, 0x04, 1, 2, 3, 4})))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("内容校验失败");
    }
}
