package com.dhy.shipmanagebackend.controller;

import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.utils.AliOssUtil;
import com.dhy.shipmanagebackend.utils.S3Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
public class FileUploadController {
    @Autowired
    private S3Util s3Util;
    @PostMapping({"/upload","/api/upload"})
    public Result<String> upload(MultipartFile file) throws Exception {
        // 1. 获取原始文件名
        String originalFilename = file.getOriginalFilename();

        // 2. 生成唯一文件名 (UUID + 后缀)，防止覆盖
        String fileName = UUID.randomUUID().toString() +
                originalFilename.substring(originalFilename.lastIndexOf("."));

        // 3. 调用工具类上传到阿里云 OSS
        String url = s3Util.uploadFile(fileName, file.getInputStream());

        // 4. 返回图片 URL 给前端
        return Result.success(url);
    }
}