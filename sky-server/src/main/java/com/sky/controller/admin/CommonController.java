package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
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
            String suffix = originalFilename.substring(originalFilename.indexOf("."));//indexOf包含


            //通过UUID生成文件Id
            UUID uuid = UUID.randomUUID();

            //合成文件名
            String fileName = uuid + suffix;

            String endPoint = "oss-cn-beijing.aliyuncs.com";
            String keyId = "LTAI5tAcMv4qghH7Qpufenxm";
            String keySecret = "BSg0QpNjoy5rzX3DBedFk72MHQPDkR";
            String bucketName = "web-tlias-mobai";
            AliOssUtil aliOssUtil = new AliOssUtil(endPoint, keyId, keySecret, bucketName);

            String upload = aliOssUtil.upload(fileBytes, fileName);

            if (upload == null || upload.length() == 0) {
                return Result.error(MessageConstant.UPLOAD_FAILED);
            } else {
                return Result.success(upload);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
