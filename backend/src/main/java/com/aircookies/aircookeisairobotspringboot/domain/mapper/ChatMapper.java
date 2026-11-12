package com.aircookies.aircookeisairobotspringboot.domain.mapper;

import com.aircookies.aircookeisairobotspringboot.domain.dos.ChatDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface ChatMapper extends BaseMapper<ChatDO> {

    /**
     * 分页查询
     * @param current
     * @param size
     * @return
     */
    default Page<ChatDO> selectPageList(Long current, Long size) {
        // 分页对象(查询第几页,每页多少数据)
        Page<ChatDO> page = new Page<>(current, size);

        // 构建查询条件
        LambdaQueryWrapper<ChatDO> wrapper = Wrappers.<ChatDO>lambdaQuery()
                .orderByDesc(ChatDO::getCreateTime); // 按照更新时间排序

        return selectPage(page, wrapper);
    }
}
