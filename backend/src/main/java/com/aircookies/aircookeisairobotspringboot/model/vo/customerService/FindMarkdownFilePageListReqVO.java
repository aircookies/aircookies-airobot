package com.aircookies.aircookeisairobotspringboot.model.vo.customerService;

import com.aircookies.aircookeisairobotspringboot.model.common.BasePageQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindMarkdownFilePageListReqVO extends BasePageQuery {
    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 起始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;
}
