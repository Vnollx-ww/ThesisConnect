package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.ProgressChain;
import com.example.thesisconnectback.entity.ProgressChainNode;
import com.example.thesisconnectback.entity.Selection;
import com.example.thesisconnectback.entity.SelectionNodeSubmission;
import com.example.thesisconnectback.mapper.ProgressChainMapper;
import com.example.thesisconnectback.mapper.ProgressChainNodeMapper;
import com.example.thesisconnectback.mapper.SelectionMapper;
import com.example.thesisconnectback.service.ProgressChainService;
import com.example.thesisconnectback.service.SelectionNodeSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProgressChainServiceImpl extends ServiceImpl<ProgressChainMapper, ProgressChain> implements ProgressChainService {

    @Autowired
    private ProgressChainNodeMapper progressChainNodeMapper;

    @Autowired
    private SelectionMapper selectionMapper;

    @Autowired
    private SelectionNodeSubmissionService selectionNodeSubmissionService;

    public static int computeProgressPercent(int completedCount, int totalNodes) {
        if (totalNodes <= 0) {
            return 0;
        }
        int c = Math.max(0, Math.min(completedCount, totalNodes));
        if (c >= totalNodes) {
            return 100;
        }
        return (int) (c * 100L / totalNodes);
    }

    @Override
    @Transactional
    public void attachDefaultChainToSelection(Selection selection) {
        if (selection == null || selection.getId() == null) {
            return;
        }
        ProgressChain def = getOne(new QueryWrapper<ProgressChain>().eq("is_default", 1).last("LIMIT 1"));
        if (def == null) {
            return;
        }
        int n = countNodes(def.getId());
        selection.setProgressChainId(def.getId());
        selection.setProgressCompletedCount(0);
        selection.setProgress(computeProgressPercent(0, n));
        selection.setUpdateTime(LocalDateTime.now());
        selectionMapper.updateById(selection);
    }

    @Override
    public int countNodes(Long chainId) {
        if (chainId == null) {
            return 0;
        }
        return Math.toIntExact(progressChainNodeMapper.selectCount(
                new QueryWrapper<ProgressChainNode>().eq("chain_id", chainId)));
    }

    @Override
    public List<ProgressChainNode> listNodesOrdered(Long chainId) {
        if (chainId == null) {
            return Collections.emptyList();
        }
        return progressChainNodeMapper.selectList(
                new QueryWrapper<ProgressChainNode>()
                        .eq("chain_id", chainId)
                        .orderByAsc("sort_order")
                        .orderByAsc("id"));
    }

    @Override
    public Map<String, Object> buildChainView(Selection selection) {
        Map<String, Object> out = new HashMap<>();
        if (selection == null || selection.getProgressChainId() == null) {
            out.put("chainId", null);
            out.put("chainName", null);
            out.put("nodes", Collections.emptyList());
            out.put("completedCount", 0);
            out.put("totalNodes", 0);
            out.put("progressPercent", selection != null && selection.getProgress() != null ? selection.getProgress() : 0);
            out.put("currentNodeSubmission", null);
            return out;
        }
        ProgressChain chain = getById(selection.getProgressChainId());
        List<ProgressChainNode> ordered = listNodesOrdered(selection.getProgressChainId());
        int n = ordered.size();
        int completed = selection.getProgressCompletedCount() != null ? selection.getProgressCompletedCount() : 0;
        completed = Math.max(0, Math.min(completed, n));

        List<Map<String, Object>> nodes = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            ProgressChainNode node = ordered.get(i);
            String state;
            if (i < completed) {
                state = "completed";
            } else if (i == completed && completed < n) {
                state = "current";
            } else {
                state = "pending";
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", node.getId());
            row.put("title", node.getTitle());
            row.put("description", node.getDescription());
            row.put("sortOrder", node.getSortOrder());
            row.put("state", state);
            nodes.add(row);
        }

        int pct = computeProgressPercent(completed, n);

        out.put("chainId", selection.getProgressChainId());
        out.put("chainName", chain != null ? chain.getName() : "");
        out.put("nodes", nodes);
        out.put("completedCount", completed);
        out.put("totalNodes", n);
        out.put("progressPercent", pct);

        if (selection.getId() != null && n > 0 && completed < n) {
            SelectionNodeSubmission latest = selectionNodeSubmissionService.findLatest(selection.getId(), completed);
            if (latest != null) {
                Map<String, Object> sm = new LinkedHashMap<>();
                sm.put("id", latest.getId());
                sm.put("nodeIndex", latest.getNodeIndex());
                sm.put("description", latest.getDescription());
                sm.put("reportUrl", latest.getReportUrl());
                sm.put("status", latest.getStatus());
                sm.put("rejectReason", latest.getRejectReason());
                sm.put("reviewerName", latest.getReviewerName());
                sm.put("reviewTime", latest.getReviewTime());
                out.put("currentNodeSubmission", sm);
            } else {
                out.put("currentNodeSubmission", null);
            }
        } else {
            out.put("currentNodeSubmission", null);
        }
        return out;
    }

    @Override
    public List<ProgressChain> listAllChains() {
        return list(new QueryWrapper<ProgressChain>().orderByDesc("is_default").orderByAsc("id"));
    }

    @Override
    @Transactional
    public ProgressChain createChain(String name, String remark) {
        ProgressChain c = new ProgressChain();
        c.setName(name);
        c.setRemark(remark);
        c.setIsDefault(0);
        save(c);
        return c;
    }

    @Override
    public boolean updateChain(Long id, String name, String remark) {
        ProgressChain c = getById(id);
        if (c == null) {
            return false;
        }
        c.setName(name);
        c.setRemark(remark);
        return updateById(c);
    }

    @Override
    @Transactional
    public boolean deleteChain(Long id) {
        // 检查是否有选题正在使用该链路
        long usedCount = selectionMapper.selectCount(
                new QueryWrapper<Selection>().eq("progress_chain_id", id).eq("deleted", 0));
        if (usedCount > 0) {
            throw new RuntimeException("该链路正在被 " + usedCount + " 个选题使用，无法删除");
        }
        // 同时删除关联的节点
        progressChainNodeMapper.delete(
                new QueryWrapper<ProgressChainNode>().eq("chain_id", id));
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean setDefaultChain(Long id) {
        ProgressChain c = getById(id);
        if (c == null) {
            return false;
        }
        // 批量取消其他默认链路（避免 N+1 更新）
        update(new LambdaUpdateWrapper<ProgressChain>()
                .ne(ProgressChain::getId, id)
                .eq(ProgressChain::getIsDefault, 1)
                .set(ProgressChain::getIsDefault, 0));
        c.setIsDefault(1);
        return updateById(c);
    }

    @Override
    @Transactional
    public ProgressChainNode addNode(Long chainId, String title, String description, Integer sortOrder) {
        ProgressChainNode node = new ProgressChainNode();
        node.setChainId(chainId);
        node.setTitle(title);
        node.setDescription(description);
        if (sortOrder == null) {
            long cnt = progressChainNodeMapper.selectCount(new QueryWrapper<ProgressChainNode>().eq("chain_id", chainId));
            node.setSortOrder((int) cnt + 1);
        } else {
            node.setSortOrder(sortOrder);
        }
        progressChainNodeMapper.insert(node);
        return node;
    }

    @Override
    public boolean updateNode(Long nodeId, String title, String description, Integer sortOrder) {
        ProgressChainNode node = progressChainNodeMapper.selectById(nodeId);
        if (node == null) {
            return false;
        }
        node.setTitle(title);
        node.setDescription(description);
        if (sortOrder != null) {
            node.setSortOrder(sortOrder);
        }
        return progressChainNodeMapper.updateById(node) > 0;
    }

    @Override
    public boolean deleteNode(Long nodeId) {
        return progressChainNodeMapper.deleteById(nodeId) > 0;
    }
}
