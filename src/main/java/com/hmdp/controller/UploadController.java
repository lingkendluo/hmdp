package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.common.SystemConstants;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
public class UploadController {

    /** 笔记图片上传 */
    @PostMapping("/upload/blog")
    public Result uploadBlog(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        String original = file.getOriginalFilename();
        String ext = ".png";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String name = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = new File(System.getProperty("user.dir"), SystemConstants.IMAGE_UPLOAD_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            return Result.fail("创建上传目录失败");
        }
        file.transferTo(new File(dir, name));
        return Result.ok("/upload/" + name);
    }
}
