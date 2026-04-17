package com.example.thesisconnectback.controller;

import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.ProgressChain;
import com.example.thesisconnectback.entity.ProgressChainNode;
import com.example.thesisconnectback.service.ProgressChainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/progress-chains")
@CrossOrigin
public class ProgressChainController {

    @Autowired
    private ProgressChainService progressChainService;

    private boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(request.getAttribute("role"));
    }

    @GetMapping
    public Result<List<ProgressChain>> list(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.forbidden("权限不足");
        }
        return Result.success(progressChainService.listAllChains());
    }

    @PostMapping
    public Result<ProgressChain> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.forbidden("权限不足");
        }
        String name = body.get("name") != null ? body.get("name").toString().trim() : "";
        if (!StringUtils.hasText(name)) {
            return Result.badRequest("请填写链路名称");
        }
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        return Result.success(progressChainService.createChain(name, remark));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.forbidden("权限不足");
        }
        String name = body.get("name") != null ? body.get("name").toString().trim() : "";
        if (!StringUtils.hasText(name)) {
            return Result.badRequest("请填写链路名称");
        }
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        boolean ok = progressChainService.updateChain(id, name, remark);
        return ok ? Result.<Void>success("保存成功", null) : Result.error("保存失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.forbidden("权限不足");
        }
        boolean ok = progressChainService.deleteChain(id);
        return ok ? Result.<Void>success("已删除", null) : Result.error("删除失败");
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.forbidden("权限不足");
        }
        boolean ok = progressChainService.setDefaultChain(id);
        return ok ? Result.<Void>success("已设为默认", null) : Result.error("操作失败");
    }

    @GetMapping("/{id}/nodes")
    public Result<List<ProgressChainNode>> listNodes(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.forbidden("权限不足");
        }
        return Result.success(progressChainService.listNodesOrdered(id));
    }

    @PostMapping("/{id}/nodes")
    public Result<ProgressChainNode> addNode(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.forbidden("权限不足");
        }
        String title = body.get("title") != null ? body.get("title").toString().trim() : "";
        if (!StringUtils.hasText(title)) {
            return Result.badRequest("请填写节点标题");
        }
        String description = body.get("description") != null ? body.get("description").toString() : null;
        Integer sortOrder = null;
        if (body.get("sortOrder") != null) {
            try {
                sortOrder = Integer.valueOf(body.get("sortOrder").toString());
            } catch (NumberFormatException e) {
                return Result.badRequest("sortOrder 必须为整数");
            }
        }
        return Result.success(progressChainService.addNode(id, title, description, sortOrder));
    }

    @PutMapping("/nodes/{nodeId}")
    public Result<Void> updateNode(@PathVariable Long nodeId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.forbidden("权限不足");
        }
        String title = body.get("title") != null ? body.get("title").toString().trim() : "";
        if (!StringUtils.hasText(title)) {
            return Result.badRequest("请填写节点标题");
        }
        String description = body.get("description") != null ? body.get("description").toString() : null;
        Integer sortOrder = null;
        if (body.get("sortOrder") != null) {
            try {
                sortOrder = Integer.valueOf(body.get("sortOrder").toString());
            } catch (NumberFormatException e) {
                return Result.badRequest("sortOrder 必须为整数");
            }
        }
        boolean ok = progressChainService.updateNode(nodeId, title, description, sortOrder);
        return ok ? Result.<Void>success("保存成功", null) : Result.error("保存失败");
    }

    @DeleteMapping("/nodes/{nodeId}")
    public Result<Void> deleteNode(@PathVariable Long nodeId, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.forbidden("权限不足");
        }
        boolean ok = progressChainService.deleteNode(nodeId);
        return ok ? Result.<Void>success("已删除", null) : Result.error("删除失败");
    }
}
