package com.xiangyun.operation;

import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.BusinessException;
import com.xiangyun.common.SecurityHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources/{resourceId}/materials")
public class ResourceMaterialController {

    private final ResourceMaterialService materialService;

    public ResourceMaterialController(ResourceMaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public ApiResponse<List<ResourceMaterialView>> list(
            @PathVariable String resourceId,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role) {
        requireStaff(role);
        return ApiResponse.success(materialService.list(resourceId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ResourceMaterialView> upload(
            @PathVariable String resourceId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "OTHER") String category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "") String userId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "") String userName,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            HttpServletRequest request) {
        requireStaff(role);
        AdminAuditContext.before(request, Map.of("resourceId", resourceId, "category", category));
        ResourceMaterialView result = materialService.upload(
                resourceId, category, title, description, file, userId, userName);
        AdminAuditContext.targetId(request, result.id());
        AdminAuditContext.after(request, result);
        return ApiResponse.success(result);
    }

    @PutMapping("/{materialId}")
    public ApiResponse<ResourceMaterialView> updateMetadata(
            @PathVariable String resourceId,
            @PathVariable String materialId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            HttpServletRequest request) {
        requireStaff(role);
        AdminAuditContext.targetId(request, materialId);
        AdminAuditContext.before(request, materialService.detail(resourceId, materialId));
        ResourceMaterialView result = materialService.updateMetadata(resourceId, materialId, body);
        AdminAuditContext.after(request, result);
        return ApiResponse.success(result);
    }

    @PostMapping(value = "/{materialId}/replace", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ResourceMaterialView> replace(
            @PathVariable String resourceId,
            @PathVariable String materialId,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = SecurityHeaders.USER_ID, defaultValue = "") String userId,
            @RequestHeader(value = SecurityHeaders.USERNAME, defaultValue = "") String userName,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            HttpServletRequest request) {
        requireAdmin(role);
        AdminAuditContext.targetId(request, materialId);
        AdminAuditContext.before(request, materialService.detail(resourceId, materialId));
        ResourceMaterialView result = materialService.replace(resourceId, materialId, file, userId, userName);
        AdminAuditContext.after(request, result);
        return ApiResponse.success(result);
    }

    @PostMapping("/{materialId}/cover")
    public ApiResponse<ResourceMaterialView> setCover(
            @PathVariable String resourceId,
            @PathVariable String materialId,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            HttpServletRequest request) {
        requireAdmin(role);
        AdminAuditContext.targetId(request, materialId);
        AdminAuditContext.before(request, materialService.detail(resourceId, materialId));
        ResourceMaterialView result = materialService.setCover(resourceId, materialId);
        AdminAuditContext.after(request, result);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{materialId}")
    public ApiResponse<Map<String, Object>> delete(
            @PathVariable String resourceId,
            @PathVariable String materialId,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role,
            HttpServletRequest request) {
        requireAdmin(role);
        AdminAuditContext.targetId(request, materialId);
        AdminAuditContext.before(request, materialService.detail(resourceId, materialId));
        Map<String, Object> result = materialService.delete(resourceId, materialId);
        AdminAuditContext.after(request, result);
        return ApiResponse.success(result);
    }

    @GetMapping("/{materialId}/content")
    public ResponseEntity<Resource> content(
            @PathVariable String resourceId,
            @PathVariable String materialId,
            @RequestParam(defaultValue = "false") boolean download,
            @RequestHeader(value = SecurityHeaders.ROLE, defaultValue = "") String role) {
        requireStaff(role);
        MaterialContent content = materialService.content(resourceId, materialId);
        ResourceMaterialView material = content.material();
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(material.originalName(), StandardCharsets.UTF_8)
                .build();
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(material.contentType());
        } catch (Exception ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "sandbox")
                .contentType(mediaType)
                .contentLength(material.fileSize())
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePrivate())
                .body(content.resource());
    }

    private void requireStaff(String role) {
        if (!"STAFF".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(40301, "仅工作人员或管理员可访问资源材料");
        }
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(40301, "仅管理员可替换、删除或设置封面");
        }
    }
}
