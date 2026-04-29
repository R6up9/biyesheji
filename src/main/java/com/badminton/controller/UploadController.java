package com.badminton.controller;

import com.badminton.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    @PostMapping("/{type}")
    public Result<Map<String, String>> upload(@PathVariable String type,
                                               @RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("===== UploadController starting =====");
        
        String[] validTypes = {"avatar", "product", "coach", "activity"};
        boolean isValid = false;
        for (String t : validTypes) {
            if (t.equals(type)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            return Result.error("无效的文件类型");
        }

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = dateStr + "_" + UUID.randomUUID().toString().substring(0, 8);
        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String newFileName = fileName + ext;

        // 保存到项目根目录uploads
        String userDir = System.getProperty("user.dir");
        System.out.println("Upload - user.dir: " + userDir);
        
        String basePath;
        File currentDirPom = new File(userDir, "pom.xml");
        if (currentDirPom.exists()) {
            basePath = userDir;
        } else {
            basePath = userDir + File.separator + "chongxin";
        }
        
        System.out.println("Upload - basePath: " + basePath);
        
        File dir = new File(basePath + File.separator + "uploads" + File.separator + type + File.separator);
        System.out.println("Upload - directory: " + dir.getAbsolutePath());
        
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Upload - directory created: " + dir.getAbsolutePath());
        }

        File destFile = new File(dir, newFileName);
        file.transferTo(destFile);
        System.out.println("Upload - file saved: " + destFile.getAbsolutePath());
        System.out.println("Upload - file exists: " + destFile.exists());

        Map<String, String> data = new HashMap<>();
        data.put("url", "http://localhost:7777/uploads/" + type + "/" + newFileName);
        data.put("filename", newFileName);
        System.out.println("Upload - return url: " + data.get("url"));
        System.out.println("===== UploadController done =====");
        return Result.success(data);
    }
}
