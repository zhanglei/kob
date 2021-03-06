package com.ke.kob.admin.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ke.kob.admin.core.mapper.LogCollectMapper;
import com.ke.kob.admin.core.mapper.TaskRecordMapper;
import com.ke.kob.admin.core.model.db.LogCollect;
import com.ke.kob.admin.core.model.db.TaskRecord;
import com.ke.kob.admin.core.service.CollectService;
import com.ke.kob.admin.core.service.ScheduleService;
import com.ke.kob.basic.model.LogContext;
import com.ke.kob.basic.model.LogMode;
import com.ke.kob.basic.support.KobUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Author: zhaoyuguang
 * @Date: 2018/8/17 下午12:11
 */
@Service
public @Slf4j class CollectServiceImpl implements CollectService {

    @Resource
    private TaskRecordMapper taskRecordMapper;
    @Resource
    private LogCollectMapper logCollectMapper;
    @Resource(name = "scheduleService")
    private ScheduleService scheduleService;

    @Override
    public void handleLogger(LogContext context) {
        String cluster = context.getCluster();
        String taskUuid = context.getTaskUuid();
        TaskRecord taskRecord = taskRecordMapper.findByTaskUuidAndCluster(taskUuid, cluster);
        System.out.println("tr =" + JSONObject.toJSONString(taskRecord));
        if (taskRecord == null) {
            log.error("哪来的日志 " + JSONObject.toJSONString(context));
            return;
        }
        LogCollect logCollect = new LogCollect();
        logCollect.setState(context.getTaskRecordState());
        logCollect.setLogUuid(context.getLogUuid());
        logCollect.setProjectCode(context.getProjectCode());
        logCollect.setTaskUuid(context.getTaskUuid());
        logCollect.setLogMode(context.getLogMode());
        logCollect.setLogLevel(KobUtils.isEmpty(context.getLogLevel())?"":context.getLogLevel());
        logCollect.setClientIdentification(context.getClientIdentification());
        logCollect.setLogTime(new Date(context.getLogTime()));

        logCollectMapper.insertOne(logCollect, cluster);
        if (LogMode.SYSTEM.name().equals(context.getLogMode())) {
            scheduleService.handleTaskLog(context, taskRecord);
        }
    }
}
