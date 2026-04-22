package com.tju.unify.conv.memo.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MemoCreateRequest {
    private String title;
    private String content;
    private Long categoryId;
    @Schema(description = "是否置顶，默认 false")
    private Boolean pinned;
    private Integer sortOrder;
    private LocalDateTime remindAt;
    private List<MemoTaskInit> tasks;

    @Data
    public static class MemoTaskInit {
        private String title;
        private Boolean done;
        private Integer sortOrder;
    }
}
