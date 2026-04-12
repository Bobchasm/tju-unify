package com.tju.unify.conv.common.config;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.regex.Pattern;

@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class,
                        RowBounds.class, ResultHandler.class})
})
public class ReadWriteSplitInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(ReadWriteSplitInterceptor.class);

    // 预编译正则
    private static final Pattern FOR_UPDATE_PATTERN =
            Pattern.compile("FOR\\s+UPDATE", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOCK_IN_SHARE_MODE_PATTERN =
            Pattern.compile("LOCK\\s+IN\\s+SHARE\\s+MODE", Pattern.CASE_INSENSITIVE);
    private static final Pattern FOR_SHARE_PATTERN =
            Pattern.compile("FOR\\s+SHARE", Pattern.CASE_INSENSITIVE);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 如果已经手动指定了数据源，直接使用
        String current = DynamicDataSourceContextHolder.peek();
        if (current != null && !current.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("使用手动指定的数据源: {}", current);
            }
            return invocation.proceed();
        }

        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        String sql = ms.getSqlSource().getBoundSql(invocation.getArgs()[1]).getSql();

        // 判断数据源
        String dataSource = determineDataSource(ms, sql);
        DynamicDataSourceContextHolder.push(dataSource);

        if (log.isDebugEnabled()) {
            log.debug("SQL类型: {}, 数据源: {}, SQL: {}",
                    ms.getSqlCommandType(), dataSource, sql);
        }

        try {
            return invocation.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * 决定使用哪个数据源
     */
    private String determineDataSource(MappedStatement ms, String sql) {
        // 优先级1: 事务中强制走主库（保证数据一致性）
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            log.debug("检测到事务，使用主库");
            return "master";
        }

        SqlCommandType commandType = ms.getSqlCommandType();

        // 优先级2: 非SELECT操作走主库
        if (commandType != SqlCommandType.SELECT) {
            log.debug("非SELECT操作({})，使用主库", commandType);
            return "master";
        }

        // 优先级3: SELECT FOR UPDATE 等锁操作走主库
        if (sql != null && isSelectForUpdate(sql)) {
            log.debug("检测到SELECT锁语句，使用主库");
            return "master";
        }

        // 默认：普通SELECT走从库
        log.debug("普通SELECT查询，使用从库");
        return "slave";
    }

    /**
     * 判断是否是带锁的SELECT语句
     */
    private boolean isSelectForUpdate(String sql) {
        String upperSql = sql.toUpperCase();
        return FOR_UPDATE_PATTERN.matcher(upperSql).find() ||
                LOCK_IN_SHARE_MODE_PATTERN.matcher(upperSql).find() ||
                FOR_SHARE_PATTERN.matcher(upperSql).find();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}