package com.example.demo.Service.CheckFace;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckFaceServiceImpl implements CheckFaceService {

    @Override
    public int checkFaceAsync(String imgPath1, String imgPath2) throws Exception {
        // Cấu hình đường dẫn python và script
        String pythonPath = "/opt/homebrew/bin/python3"; 
        String pythonScript = "/Users/cps/Desktop/Yolo_face/Check_faced.py"; 

        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(pythonScript);
        command.add(imgPath1);
        command.add(imgPath2);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        // Đọc dữ liệu Standard Output từ Script Python trả về
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // Đọc dữ liệu Standard Error từ Script Python
        StringBuilder error = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Python exited with code " + exitCode + ".\nError: " + error + "\nOutput: " + output);
        }

        if (!error.toString().trim().isEmpty()) {
            System.out.println("Python produced warnings:\n" + error);
        }

        if (output.toString().contains("Ket_qua_khop")) {
            return 200;
        }

        throw new RuntimeException("Xác thực thất bại: Kết quả khuôn mặt không khớp");
    }
}