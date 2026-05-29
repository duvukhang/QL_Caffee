package com.example.Admin.DTOS.Respone;

import lombok.Data;
import java.util.List;

@Data // Tự động sinh Getter/Setter
public class PageResponse<T> {
    
    // Đã FIX: Dùng List<T> thay vì List<Item<T>>
    private List<T> items; 
    
    private int totalCount;
    private int pageIndex;
    private int pageSize;
    private int totalPages;
}