package com.example.demo.Service.ImgService;

import org.springframework.web.multipart.MultipartFile; // Nhớ thêm import này

public interface ImgService {
    
    // ĐÃ ĐỒNG BỘ: Hàm nhận vào MultipartFile và trả về chuỗi đường dẫn ảnh sau khi lưu
    String SaveImgIntoProject(MultipartFile file) throws Exception;
}