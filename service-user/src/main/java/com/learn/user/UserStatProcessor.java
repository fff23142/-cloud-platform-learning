package com.learn.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

// PowerJob 任务处理器：定时执行用户统计
// 实现 BasicProcessor 接口，控制台配置定时规则后自动触发
@Component
public class UserStatProcessor implements BasicProcessor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger logger = context.getOmsLogger();
        // OmsLogger = PowerJob 提供的日志，会实时显示在控制台

        logger.info("===== 开始执行用户统计任务 =====");

        // 查询用户总数
        Long count = userMapper.selectCount(null);
        logger.info("当前用户总数：{}", count);

        // 查所有用户
        var users = userMapper.selectList(null);
        for (var user : users) {
            logger.info("用户：{} | 邮箱：{} | 余额：{}",
                    user.getName(), user.getEmail(), user.getBalance());
        }

        logger.info("===== 用户统计任务执行完毕 =====");
        return new ProcessResult(true, "统计完成，共 " + count + " 个用户");
        // 返回 true = 执行成功，false = 失败（触发重试）
    }
}
