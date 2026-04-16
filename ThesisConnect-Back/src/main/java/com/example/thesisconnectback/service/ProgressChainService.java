package com.example.thesisconnectback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.thesisconnectback.entity.ProgressChain;
import com.example.thesisconnectback.entity.ProgressChainNode;
import com.example.thesisconnectback.entity.Selection;

import java.util.List;
import java.util.Map;

public interface ProgressChainService extends IService<ProgressChain> {

    void attachDefaultChainToSelection(Selection selection);

    int countNodes(Long chainId);

    List<ProgressChainNode> listNodesOrdered(Long chainId);

    Map<String, Object> buildChainView(Selection selection);

    List<ProgressChain> listAllChains();

    ProgressChain createChain(String name, String remark);

    boolean updateChain(Long id, String name, String remark);

    boolean deleteChain(Long id);

    boolean setDefaultChain(Long id);

    ProgressChainNode addNode(Long chainId, String title, String description, Integer sortOrder);

    boolean updateNode(Long nodeId, String title, String description, Integer sortOrder);

    boolean deleteNode(Long nodeId);
}
