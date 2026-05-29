package com.example.Admin.Service.ImgService;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImgServiceImpl implements ImgService {

    // Đường dẫn thư mục lưu ảnh trong project (ví dụ: thư mục "uploads" ở góc project)
    private final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

    @Override
    public String SaveImgIntoProject(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return "default-avatar.png"; // Trả về ảnh mặc định nếu không up file
        }

        try {
            // 1. Tạo thư mục lưu trữ nếu chưa tồn tại
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 2. Tạo tên file duy nhất bằng UUID để tránh bị trùng tên ảnh
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // 3. Thực hiện lưu file vào đĩa cứng
            Path targetPath = Paths.get(UPLOAD_DIR + File.separator + uniqueFileName);
            Files.copy(file.getInputStream(), targetPath);

            // 4. Trả về đường dẫn tương đối để lưu vào cột Avatar dưới Database
            return "uploads/" + uniqueFileName;

        } catch (Exception e) {
            throw new Exception("Lỗi trong quá trình lưu file ảnh: " + e.getMessage());
        }
    }
}