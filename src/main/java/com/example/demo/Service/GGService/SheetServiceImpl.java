package com.example.demo.Service.GGService;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SheetServiceImpl implements SheetService {

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String APPLICATION_NAME = "QuanLyChuoiCuaHang";

    @Value("${GGSheetAPIKey}")
    private String pathToCredential;

    @Value("${SheetId}")
    private String spreadsheetId;

    // ĐÃ FIX: Đổi tên hàm thành StoreReview và kiểu trả về thành List<String[]> để khớp 100% với Interface và Controller
    @Override
    public List<String[]> StoreReview(String storeId) throws Exception {
        List<String[]> resultData = new ArrayList<>();

        // Đọc file ủy quyền credentials JSON
        GoogleCredentials credentials;
        try (FileInputStream in = new FileInputStream(pathToCredential)) {
            credentials = GoogleCredentials.fromStream(in).createScoped(SCOPES);
        }

        // Khởi tạo đối tượng kết nối với API Google Sheets
        Sheets sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        String range = "sheet1!A:F";

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values != null && values.size() > 1) {
            // Vòng lặp chạy từ index 1 nhằm mục đích bỏ qua dòng tiêu đề đầu tiên (.Skip(1) trong LINQ)
            for (int i = 1; i < values.size(); i++) {
                List<Object> row = values.get(i);
                
                // Logic Filter: Kiểm tra xem hàng có đủ 6 cột, cột thứ 6 khác null và có giá trị trùng khớp với storeId hay không
                if (row.size() >= 6 && row.get(5) != null && row.get(5).toString().equals(storeId)) {
                    
                    // Khởi tạo mảng String cố định có kích thước bằng chính số phần tử của dòng để tối ưu bộ nhớ
                    String[] stringRow = new String[row.size()];
                    
                    for (int j = 0; j < row.size(); j++) {
                        Object cell = row.get(j);
                        stringRow[j] = (cell != null) ? cell.toString() : "";
                    }
                    
                    resultData.add(stringRow);
                }
            }
        }
        return resultData;
    }
}