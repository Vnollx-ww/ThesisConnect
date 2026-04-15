package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.SystemConfig;
import com.example.thesisconnectback.mapper.SystemConfigMapper;
import com.example.thesisconnectback.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    public static final String KEY_SELECTION_START = "selection_start_time";
    public static final String KEY_SELECTION_END = "selection_end_time";
    public static final String KEY_MAX_SELECTIONS = "max_selections_per_student";

    private static final String DEFAULT_MAX = "3";

    @Override
    public Map<String, String> getAllConfig() {
        List<SystemConfig> list = list();
        return list.stream().collect(Collectors.toMap(SystemConfig::getConfigKey, SystemConfig::getConfigValue, (a, b) -> a));
    }

    @Override
    @Transactional
    public void saveConfig(Map<String, String> config) {
        if (config == null || config.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> e : config.entrySet()) {
            if (e.getKey() == null) {
                continue;
            }
            SystemConfig existing = getById(e.getKey());
            if (existing != null) {
                existing.setConfigValue(e.getValue());
                updateById(existing);
            } else {
                SystemConfig row = new SystemConfig();
                row.setConfigKey(e.getKey());
                row.setConfigValue(e.getValue());
                save(row);
            }
        }
    }

    @Override
    public boolean isWithinSelectionPeriod() {
        Map<String, String> m = getAllConfig();
        String startStr = m.get(KEY_SELECTION_START);
        String endStr = m.get(KEY_SELECTION_END);
        if (startStr == null || startStr.isBlank() || endStr == null || endStr.isBlank()) {
            return true;
        }
        try {
            LocalDateTime start = LocalDateTime.parse(startStr.trim());
            LocalDateTime end = LocalDateTime.parse(endStr.trim());
            LocalDateTime now = LocalDateTime.now();
            return !now.isBefore(start) && !now.isAfter(end);
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    @Override
    public int getMaxSelectionsPerStudent() {
        Map<String, String> m = getAllConfig();
        String v = m.get(KEY_MAX_SELECTIONS);
        if (v == null || v.isBlank()) {
            return Integer.parseInt(DEFAULT_MAX);
        }
        try {
            int n = Integer.parseInt(v.trim());
            return Math.max(1, Math.min(n, 99));
        } catch (NumberFormatException e) {
            return Integer.parseInt(DEFAULT_MAX);
        }
    }

    @Override
    public Map<String, String> getPublicSelectionRules() {
        Map<String, String> m = getAllConfig();
        Map<String, String> out = new HashMap<>();
        out.put(KEY_SELECTION_START, m.getOrDefault(KEY_SELECTION_START, ""));
        out.put(KEY_SELECTION_END, m.getOrDefault(KEY_SELECTION_END, ""));
        out.put(KEY_MAX_SELECTIONS, String.valueOf(getMaxSelectionsPerStudent()));
        out.put("selectionOpen", String.valueOf(isWithinSelectionPeriod()));
        return out;
    }
}
