package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * ClassName: CommonController
 *
 * @Author Mobai
 * @Create 2023/11/10 8:57
 * @Version 1.0
 * Description:
 */

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/admin/common")
public class CommonController {

    //注入属性配置类
    @Autowired
    private AliOssProperties aliOssProperties;

    /**
     * 文件上传
     *
     * @return
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(MultipartFile file) {

        try {
            //获取文件byte
            byte[] fileBytes = file.getBytes();

            //获取文件原始名字
            String originalFilename = file.getOriginalFilename();

            //分割文件名，获取后缀
            String suffix = originalFilename.substring(originalFilename.indexOf("."));

            //通过UUID生成文件Id
            UUID uuid = UUID.randomUUID();

            //合成文件名
            String fileName = uuid + suffix;

            String endPoint = aliOssProperties.getEndpoint();
            String keyId = aliOssProperties.getAccessKeyId();
            String keySecret = aliOssProperties.getAccessKeySecret();
            String bucketName = aliOssProperties.getBucketName();
            AliOssUtil aliOssUtil = new AliOssUtil(endPoint, keyId, keySecret, bucketName);

            String upload = aliOssUtil.upload(fileBytes, fileName);

            if (upload == null || StringUtils.isEmpty(upload)) {
                //返回上传失败
                return Result.error(MessageConstant.UPLOAD_FAILED);
            } else {
                //上传成功
                return Result.success(upload);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
