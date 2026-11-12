package com.aircookies.aircookeisairobotspringboot.service;

import com.aircookies.aircookeisairobotspringboot.model.vo.customerService.DeleteMarkdownFileReqVO;
import com.aircookies.aircookeisairobotspringboot.model.vo.customerService.FindMarkdownFilePageListReqVO;
import com.aircookies.aircookeisairobotspringboot.model.vo.customerService.FindMarkdownFilePageListRspVO;
import com.aircookies.aircookeisairobotspringboot.model.vo.customerService.UpdateMarkdownFileReqVO;
import com.aircookies.aircookeisairobotspringboot.utils.PageResponse;
import com.aircookies.aircookeisairobotspringboot.utils.Response;
import org.springframework.web.multipart.MultipartFile;

public interface CustomerService {

    /**
     * 上传Markdown问答文件
     * @param file
     * @return
     */
    Response<?> uploadMarkdownFile(MultipartFile file);

    /**
     * 删除 Markdown 问答文件
     * @param deleteMarkdownFileReqVO
     * @return
     */
    Response<?> deleteMarkdownFile(DeleteMarkdownFileReqVO deleteMarkdownFileReqVO);

    /**
     * 分页查询 Markdown 问答文件
     * @param findMarkdownFilePageListReqVO
     * @return
     */
    PageResponse<FindMarkdownFilePageListRspVO> findMarkdownFilePageList(FindMarkdownFilePageListReqVO findMarkdownFilePageListReqVO);

    /**
     * 修改  Markdown 问答文件信息
     * @param updateMarkdownFileReqVO
     * @return
     */
    Response<?> updateMarkdownFile(UpdateMarkdownFileReqVO updateMarkdownFileReqVO);
}
