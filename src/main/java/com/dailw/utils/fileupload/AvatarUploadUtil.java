package com.dailw.utils.fileupload;

import com.dailw.common.ErrorCode;
import com.dailw.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 头像上传工具类
 * 提供头像文件的上传、验证、存储功能
 * 
 * @author trave
 */
@Component
@Slf4j
public class AvatarUploadUtil {

    /**
     * 允许的图片格式（从配置文件读取）
     */
    @Value("${file.upload.avatar.allowed-extensions:jpg,jpeg,png,gif,bmp,webp}")
    private String allowedExtensionsStr;
    
    /**
     * 允许的MIME类型（从配置文件读取）
     */
    @Value("${file.upload.avatar.allowed-mime-types:image/jpeg,image/png,image/gif,image/bmp,image/webp}")
    private String allowedMimeTypesStr;
    
    /**
     * 最大文件大小（从配置文件读取，默认5MB）
     */
    @Value("${file.upload.avatar.max-file-size:5242880}")
    private long maxFileSize;

    /**
     * 上传目录（从配置文件读取，默认为项目根目录下的uploads/avatar/）
     */
    @Value("${file.upload.avatar.path:uploads/avatar/}")
    private String uploadPath;

    /**
     * 访问URL前缀（从配置文件读取）
     */
    @Value("${file.upload.avatar.url-prefix:/dailw/uploads/avatar/}")
    private String urlPrefix;

    /**
     * 上传头像文件
     * 
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    public String uploadAvatar(MultipartFile file, Long userId) {
        // 1. 验证文件
        validateFile(file);
        
        // 2. 生成文件名
        String fileName = generateFileName(file, userId);
        
        // 3. 确保上传目录存在
        ensureUploadDirectoryExists();
        
        // 4. 保存文件
        String filePath = saveFile(file, fileName);
        
        // 5. 返回访问URL
        String accessUrl = urlPrefix + fileName;
        
        log.info("用户 {} 头像上传成功，文件路径: {}, 访问URL: {}", userId, filePath, accessUrl);
        
        return accessUrl;
    }

    /**
     * 验证上传的文件
     * 
     * @param file 上传的文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要上传的头像文件");
        }

        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                    String.format("头像文件大小不能超过 %dMB", maxFileSize / 1024 / 1024));
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = getAllowedExtensions();
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                    "不支持的文件格式，请上传 " + String.join(", ", allowedExtensions) + " 格式的图片");
        }

        // 检查MIME类型
        String contentType = file.getContentType();
        List<String> allowedMimeTypes = getAllowedMimeTypes();
        if (contentType == null || !allowedMimeTypes.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件类型");
        }
    }

    /**
     * 生成唯一的文件名
     * 
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 生成的文件名
     */
    private String generateFileName(MultipartFile file, Long userId) {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 生成文件名格式：avatar_用户ID_时间戳_随机UUID.扩展名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        
        return String.format("avatar_%d_%s_%s.%s", userId, timestamp, uuid, extension);
    }

    /**
     * 获取文件扩展名
     * 
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 确保上传目录存在
     */
    private void ensureUploadDirectoryExists() {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建上传目录: {}", uploadDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建上传目录失败");
        }
    }

    /**
     * 保存文件到磁盘
     * 
     * @param file 上传的文件
     * @param fileName 文件名
     * @return 文件的完整路径
     */
    private String saveFile(MultipartFile file, String fileName) {
        try {
            Path filePath = Paths.get(uploadPath, fileName);
            file.transferTo(filePath.toFile());
            
            log.debug("文件保存成功: {}", filePath.toAbsolutePath());
            
            return filePath.toString();
        } catch (IOException e) {
            log.error("保存文件失败: {}", fileName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
        }
    }

    /**
     * 删除旧的头像文件
     * 
     * @param avatarUrl 头像URL
     */
    public void deleteOldAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return;
        }
        
        try {
            // 从URL中提取文件名
            String fileName = extractFileNameFromUrl(avatarUrl);
            if (fileName != null) {
                Path filePath = Paths.get(uploadPath, fileName);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("删除旧头像文件: {}", filePath.toAbsolutePath());
                }
            }
        } catch (Exception e) {
            // 删除失败不影响主流程，只记录日志
            log.warn("删除旧头像文件失败: {}", avatarUrl, e);
        }
    }

    /**
     * 从URL中提取文件名
     * 
     * @param url 文件URL
     * @return 文件名
     */
    private String extractFileNameFromUrl(String url) {
        if (url == null || !url.contains(urlPrefix)) {
            return null;
        }
        
        int index = url.lastIndexOf("/");
        if (index >= 0 && index < url.length() - 1) {
            return url.substring(index + 1);
        }
        
        return null;
    }

    /**
     * 检查文件是否存在
     * 
     * @param fileName 文件名
     * @return 是否存在
     */
    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        Path filePath = Paths.get(uploadPath, fileName);
        return Files.exists(filePath);
    }
    
    /**
     * 获取允许的文件扩展名列表
     * 
     * @return 允许的扩展名列表
     */
    private List<String> getAllowedExtensions() {
        if (allowedExtensionsStr == null || allowedExtensionsStr.trim().isEmpty()) {
            return Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
        }
        return Arrays.asList(allowedExtensionsStr.split(","));
    }
    
    /**
     * 获取允许的MIME类型列表
     * 
     * @return 允许的MIME类型列表
     */
    private List<String> getAllowedMimeTypes() {
        if (allowedMimeTypesStr == null || allowedMimeTypesStr.trim().isEmpty()) {
            return Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp");
        }
        return Arrays.asList(allowedMimeTypesStr.split(","));
    }
}