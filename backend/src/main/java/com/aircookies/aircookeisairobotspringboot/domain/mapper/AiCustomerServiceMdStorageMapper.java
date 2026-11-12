package com.aircookies.aircookeisairobotspringboot.domain.mapper;

import com.aircookies.aircookeisairobotspringboot.domain.dos.AiCustomerServiceMdStorageDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Objects;

public interface AiCustomerServiceMdStorageMapper extends BaseMapper<AiCustomerServiceMdStorageDO> {

    /**
     * 分页查询
     * @param current
     * @param size
     * @return
     */
    default Page<AiCustomerServiceMdStorageDO> selectPageList(Long current, Long size, String fileName, LocalDate startDate, LocalDate endDate) {
        // 分页对象(查询第几页、每页多少数据)
        Page<AiCustomerServiceMdStorageDO> page = new Page<>(current, size);

        // 构建查询条件
        LambdaQueryWrapper<AiCustomerServiceMdStorageDO> wrapper = Wrappers.<AiCustomerServiceMdStorageDO>lambdaQuery()
                .like(StringUtils.isNotBlank(fileName), AiCustomerServiceMdStorageDO::getOriginalFileName, fileName) // like 模块查询
                .ge(Objects.nonNull(startDate), AiCustomerServiceMdStorageDO::getCreateTime, startDate) // 大于等于 startDate
                .le(Objects.nonNull(endDate), AiCustomerServiceMdStorageDO::getCreateTime, endDate)  // 小于等于 endDate
                .orderByDesc(AiCustomerServiceMdStorageDO::getCreateTime); // 按创建时间倒叙

        return selectPage(page, wrapper);
    }
}
