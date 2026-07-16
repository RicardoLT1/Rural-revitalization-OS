package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ResourceMaterialService {

    public static final Set<String> CATEGORIES = Set.of(
            "FIELD_PHOTO", "OWNERSHIP", "INVESTMENT", "APPROVAL", "OTHER");

    private static final String MATERIAL_SELECT = """
            select m.id,m.resource_id,m.category,m.title,m.description,m.is_cover,
                   m.uploaded_by,m.uploaded_by_name,m.created_at,m.updated_at,
                   f.original_name,f.content_type,f.file_size,f.sha256
            from resource_material m
            join file_object f on f.id=m.file_id
            where m.deleted_at is null and f.deleted_at is null
            """;

    private final JdbcTemplate jdbcTemplate;
    private final LocalStorageService storageService;

    public ResourceMaterialService(JdbcTemplate jdbcTemplate, LocalStorageService storageService) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageService = storageService;
    }

    public List<ResourceMaterialView> list(String resourceId) {
        long actualResourceId = resourceId(resourceId);
        assertResource(actualResourceId);
        return jdbcTemplate.query(
                MATERIAL_SELECT + " and m.resource_id=? order by m.is_cover desc,m.sort_order,m.created_at desc",
                (rs, rowNum) -> material(rs), actualResourceId);
    }

    @Transactional
    public ResourceMaterialView upload(String resourceId,
                                       String category,
                                       String title,
                                       String description,
                                       MultipartFile file,
                                       String userId,
                                       String userName) {
        long actualResourceId = resourceId(resourceId);
        assertResource(actualResourceId);
        String actualCategory = category(category);
        StoredFile stored = storageService.store(file);
        requireCategoryCompatible(actualCategory, stored);
        registerRollbackCleanup(stored.storageKey());
        try {
            long fileId = insertFile(stored, userId, userName);
            boolean cover = "FIELD_PHOTO".equals(actualCategory)
                    && count("select count(*) from resource_material where resource_id=? and is_cover=1 and deleted_at is null", actualResourceId) == 0;
            int sortOrder = count("select count(*) from resource_material where resource_id=? and deleted_at is null", actualResourceId);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                        insert into resource_material(resource_id,file_id,category,title,description,is_cover,sort_order,uploaded_by,uploaded_by_name)
                        values(?,?,?,?,?,?,?,?,?)
                        """, Statement.RETURN_GENERATED_KEYS);
                statement.setLong(1, actualResourceId);
                statement.setLong(2, fileId);
                statement.setString(3, actualCategory);
                statement.setString(4, text(title, stored.originalName(), 128));
                statement.setString(5, text(description, null, 512));
                statement.setBoolean(6, cover);
                statement.setInt(7, sortOrder);
                statement.setString(8, limit(userId, 64));
                statement.setString(9, limit(userName, 128));
                return statement;
            }, keyHolder);
            return detail(actualResourceId, generatedId(keyHolder));
        } catch (RuntimeException ex) {
            storageService.deletePhysical(stored.storageKey());
            throw ex;
        }
    }

    @Transactional
    public ResourceMaterialView updateMetadata(String resourceId,
                                               String materialId,
                                               Map<String, Object> body) {
        long actualResourceId = resourceId(resourceId);
        long actualMaterialId = materialId(materialId);
        ResourceMaterialView current = detail(actualResourceId, actualMaterialId);
        String title = text(String.valueOf(body.getOrDefault("title", current.title())), current.title(), 128);
        Object requestedDescription = body.get("description");
        String description = requestedDescription == null ? current.description()
                : text(String.valueOf(requestedDescription), null, 512);
        jdbcTemplate.update("update resource_material set title=?,description=? where id=? and resource_id=? and deleted_at is null",
                title, description, actualMaterialId, actualResourceId);
        return detail(actualResourceId, actualMaterialId);
    }

    @Transactional
    public ResourceMaterialView replace(String resourceId,
                                        String materialId,
                                        MultipartFile file,
                                        String userId,
                                        String userName) {
        long actualResourceId = resourceId(resourceId);
        long actualMaterialId = materialId(materialId);
        ResourceMaterialView current = detail(actualResourceId, actualMaterialId);
        StoredFile stored = storageService.store(file);
        requireCategoryCompatible(current.category(), stored);
        registerRollbackCleanup(stored.storageKey());
        try {
            Long oldFileId = jdbcTemplate.queryForObject(
                    "select file_id from resource_material where id=? and resource_id=? and deleted_at is null",
                    Long.class, actualMaterialId, actualResourceId);
            long newFileId = insertFile(stored, userId, userName);
            boolean keepCover = current.cover() && stored.contentType().startsWith("image/");
            jdbcTemplate.update("""
                    update resource_material set file_id=?,title=?,is_cover=?,uploaded_by=?,uploaded_by_name=?
                    where id=? and resource_id=? and deleted_at is null
                    """, newFileId, current.title(), keepCover, limit(userId, 64), limit(userName, 128),
                    actualMaterialId, actualResourceId);
            if (oldFileId != null) {
                jdbcTemplate.update("update file_object set deleted_at=current_timestamp where id=? and deleted_at is null", oldFileId);
            }
            if (current.cover() && !keepCover) promoteCover(actualResourceId);
            return detail(actualResourceId, actualMaterialId);
        } catch (RuntimeException ex) {
            storageService.deletePhysical(stored.storageKey());
            throw ex;
        }
    }

    @Transactional
    public ResourceMaterialView setCover(String resourceId, String materialId) {
        long actualResourceId = resourceId(resourceId);
        long actualMaterialId = materialId(materialId);
        ResourceMaterialView material = detail(actualResourceId, actualMaterialId);
        if (!material.image() || !"FIELD_PHOTO".equals(material.category())) {
            throw new BusinessException(40029, "只有现场照片分类的图片可以设为资源封面");
        }
        jdbcTemplate.update("update resource_material set is_cover=0 where resource_id=? and deleted_at is null", actualResourceId);
        jdbcTemplate.update("update resource_material set is_cover=1 where id=? and resource_id=? and deleted_at is null",
                actualMaterialId, actualResourceId);
        return detail(actualResourceId, actualMaterialId);
    }

    @Transactional
    public Map<String, Object> delete(String resourceId, String materialId) {
        long actualResourceId = resourceId(resourceId);
        long actualMaterialId = materialId(materialId);
        ResourceMaterialView current = detail(actualResourceId, actualMaterialId);
        Long fileId = jdbcTemplate.queryForObject(
                "select file_id from resource_material where id=? and resource_id=? and deleted_at is null",
                Long.class, actualMaterialId, actualResourceId);
        jdbcTemplate.update("update resource_material set deleted_at=current_timestamp,is_cover=0 where id=? and resource_id=? and deleted_at is null",
                actualMaterialId, actualResourceId);
        if (fileId != null) {
            jdbcTemplate.update("update file_object set deleted_at=current_timestamp where id=? and deleted_at is null", fileId);
        }
        if (current.cover()) promoteCover(actualResourceId);
        return Map.of("id", String.valueOf(actualMaterialId), "resourceId", String.valueOf(actualResourceId), "deleted", true);
    }

    public MaterialContent content(String resourceId, String materialId) {
        long actualResourceId = resourceId(resourceId);
        long actualMaterialId = materialId(materialId);
        ResourceMaterialView material = detail(actualResourceId, actualMaterialId);
        String storageKey = jdbcTemplate.queryForObject("""
                select f.storage_key from resource_material m join file_object f on f.id=m.file_id
                where m.id=? and m.resource_id=? and m.deleted_at is null and f.deleted_at is null
                """, String.class, actualMaterialId, actualResourceId);
        return new MaterialContent(material, storageService.open(storageKey));
    }

    public ResourceMaterialView detail(String resourceId, String materialId) {
        return detail(resourceId(resourceId), materialId(materialId));
    }

    private ResourceMaterialView detail(long resourceId, long materialId) {
        List<ResourceMaterialView> rows = jdbcTemplate.query(
                MATERIAL_SELECT + " and m.id=? and m.resource_id=?",
                (rs, rowNum) -> material(rs), materialId, resourceId);
        if (rows.isEmpty()) throw new BusinessException(40422, "资源材料不存在");
        return rows.get(0);
    }

    private ResourceMaterialView material(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new ResourceMaterialView(
                String.valueOf(rs.getLong("id")),
                String.valueOf(rs.getLong("resource_id")),
                rs.getString("category"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBoolean("is_cover"),
                rs.getString("original_name"),
                rs.getString("content_type"),
                rs.getLong("file_size"),
                rs.getString("sha256"),
                rs.getString("uploaded_by"),
                rs.getString("uploaded_by_name"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime());
    }

    private long insertFile(StoredFile stored, String userId, String userName) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into file_object(storage_key,original_name,content_type,file_size,sha256,created_by,created_by_name)
                    values(?,?,?,?,?,?,?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, stored.storageKey());
            statement.setString(2, stored.originalName());
            statement.setString(3, stored.contentType());
            statement.setLong(4, stored.size());
            statement.setString(5, stored.sha256());
            statement.setString(6, limit(userId, 64));
            statement.setString(7, limit(userName, 128));
            return statement;
        };
        jdbcTemplate.update(creator, keyHolder);
        return generatedId(keyHolder);
    }

    private long generatedId(KeyHolder keyHolder) {
        Map<String, Object> keys = keyHolder.getKeys();
        Number key = keys == null ? null : keys.entrySet().stream()
                .filter(entry -> "id".equalsIgnoreCase(entry.getKey())
                        || "generated_key".equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getValue)
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .findFirst()
                .orElse(null);
        if (key == null && keys != null) {
            List<Number> numericKeys = keys.values().stream()
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .toList();
            if (numericKeys.size() == 1) key = numericKeys.get(0);
        }
        if (key == null) throw new BusinessException(50022, "材料记录创建失败");
        return key.longValue();
    }

    private void promoteCover(long resourceId) {
        List<Long> candidates = jdbcTemplate.query("""
                select m.id from resource_material m join file_object f on f.id=m.file_id
                where m.resource_id=? and m.category='FIELD_PHOTO' and m.deleted_at is null
                  and f.deleted_at is null and f.content_type like 'image/%'
                order by m.sort_order,m.created_at limit 1
                """, (rs, rowNum) -> rs.getLong(1), resourceId);
        if (!candidates.isEmpty()) {
            jdbcTemplate.update("update resource_material set is_cover=1 where id=?", candidates.get(0));
        }
    }

    private void requireCategoryCompatible(String category, StoredFile stored) {
        if ("FIELD_PHOTO".equals(category) && !stored.contentType().startsWith("image/")) {
            storageService.deletePhysical(stored.storageKey());
            throw new BusinessException(40030, "现场照片分类仅支持 JPG、PNG 或 WEBP 图片");
        }
    }

    private void assertResource(long resourceId) {
        if (count("select count(*) from resource where id=? and coalesce(status,'')<>'deleted'", resourceId) == 0) {
            throw new BusinessException(40400, "资源不存在");
        }
    }

    private int count(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    private long resourceId(String value) {
        return positiveId(value, "资源编号不合法");
    }

    private long materialId(String value) {
        return positiveId(value, "材料编号不合法");
    }

    private long positiveId(String value, String message) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (Exception ex) {
            throw new BusinessException(40026, message);
        }
    }

    private String category(String category) {
        String value = StringUtils.hasText(category) ? category.trim().toUpperCase() : "OTHER";
        if (!CATEGORIES.contains(value)) throw new BusinessException(40027, "材料分类不合法");
        return value;
    }

    private String text(String value, String fallback, int maxLength) {
        String actual = StringUtils.hasText(value) ? value.trim() : fallback;
        if (actual == null) return null;
        if (actual.length() > maxLength) throw new BusinessException(40028, "材料文字信息过长");
        return actual;
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength);
    }

    private void registerRollbackCleanup(String storageKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) storageService.deletePhysical(storageKey);
            }
        });
    }
}
