package com.example.Admin.DTOS.Respone;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class PageResponse3<T> {
    private int pageIndex;
    private int pageSize;
    private int totalPages;
    private int totalCount;
    private List<T> items = new ArrayList<>();

    public boolean isHasPreviousPage() { return pageIndex > 1; }
    public boolean isHasNextPage() { return pageIndex < totalPages; }
}
