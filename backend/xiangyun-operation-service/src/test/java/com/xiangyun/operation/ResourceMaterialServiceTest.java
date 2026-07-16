package com.xiangyun.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceMaterialServiceTest {

    @TempDir
    Path root;

    private ResourceMaterialService service;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:material-" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("create table resource(id bigint primary key,status varchar(32))");
        jdbc.execute("""
                create table file_object(
                  id bigint auto_increment primary key,storage_key varchar(255),original_name varchar(255),
                  content_type varchar(128),file_size bigint,sha256 varchar(64),created_by varchar(64),
                  created_by_name varchar(128),created_at timestamp default current_timestamp,deleted_at timestamp)
                """);
        jdbc.execute("""
                create table resource_material(
                  id bigint auto_increment primary key,resource_id bigint,file_id bigint,category varchar(32),
                  title varchar(128),description varchar(512),is_cover boolean default false,sort_order int,
                  uploaded_by varchar(64),uploaded_by_name varchar(128),created_at timestamp default current_timestamp,
                  updated_at timestamp default current_timestamp,deleted_at timestamp)
                """);
        jdbc.update("insert into resource(id,status) values(101,'active')");
        service = new ResourceMaterialService(jdbc, new LocalStorageService(root, 1024 * 1024));
    }

    @Test
    void managesPhotoMetadataCoverAndSoftDelete() {
        byte[] jpeg = new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 1, 2, 3, 4};
        ResourceMaterialView created = service.upload("101", "FIELD_PHOTO", "院落正面", "改造前现场",
                new MockMultipartFile("file", "front.jpg", "image/jpeg", jpeg), "3", "admin");

        assertThat(created.cover()).isTrue();
        assertThat(service.list("101")).hasSize(1);

        ResourceMaterialView updated = service.updateMetadata("101", created.id(),
                Map.of("title", "院落全景", "description", "2026 年踏勘记录"));
        assertThat(updated.title()).isEqualTo("院落全景");
        assertThat(updated.description()).contains("踏勘");

        Map<String, Object> deleted = service.delete("101", created.id());
        assertThat(deleted.get("deleted")).isEqualTo(true);
        assertThat(service.list("101")).isEmpty();
    }

    @Test
    void fieldPhotoCategoryRejectsDocumentFiles() {
        byte[] pdf = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2d, 1, 2, 3};

        assertThatThrownBy(() -> service.upload("101", "FIELD_PHOTO", "错误分类", null,
                new MockMultipartFile("file", "proof.pdf", "application/pdf", pdf), "3", "admin"))
                .isInstanceOf(com.xiangyun.common.BusinessException.class)
                .hasMessageContaining("现场照片分类");
        assertThat(service.list("101")).isEmpty();
    }
}
