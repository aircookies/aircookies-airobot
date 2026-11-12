package com.aircookies.aircookeisairobotspringboot.service;

import com.aircookies.aircookeisairobotspringboot.model.vo.chat.*;
import com.aircookies.aircookeisairobotspringboot.utils.PageResponse;
import com.aircookies.aircookeisairobotspringboot.utils.Response;

public interface ChatService {

    /**
     * 新建对话
     * @param newChatReqVO
     * @return
     */
    Response<NewChatRspVO> newChat(NewChatReqVO newChatReqVO);

    /**
     * 查询历史信息
     * @param findChatHistoryMessagePageListReqVO
     * @return
     */
    PageResponse<FindChatHistoryMessagePageListRspVO> findChatHistoryMessagePageList(FindChatHistoryMessagePageListReqVO findChatHistoryMessagePageListReqVO);

    /**
     * 查询历史对话
     *
     * @param findChatHistoryPageListReqVO
     * @return
     */
    PageResponse<FindChatHistoryPageListRspVO> findChatHistoryPageList(FindChatHistoryPageListReqVO findChatHistoryPageListReqVO);

    /**
     * 重命名对话摘要
     * @param renameChatReqVO
     * @return
     */
    Response<?> renameChatSummary(RenameChatReqVO renameChatReqVO);

    /**
     * 删除对话
     * @param deleteChatReqVO
     * @return
     */
    Response<?> deleteChat(DeleteChatReqVO deleteChatReqVO);
}
