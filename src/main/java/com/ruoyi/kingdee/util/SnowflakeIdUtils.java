package com.ruoyi.kingdee.util;

import java.lang.management.ManagementFactory;

/**
 * 雪花算法 ID 生成器。
 *
 * <p>用于生成 19 位左右的趋势递增主键。默认按主机名 + 进程号推导 dataCenterId / workerId，
 * 多实例部署建议通过 JVM 参数显式指定，避免机器号碰撞：</p>
 *
 * <pre>-Dsnowflake.dataCenterId=1 -Dsnowflake.workerId=1</pre>
 */
public final class SnowflakeIdUtils {

    /** 起始时间戳：2010-11-04 01:42:54.657 UTC */
    private static final long START_TIMESTAMP = 1288834974657L;

    private static final long DATA_CENTER_ID_BITS = 5L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_TIMESTAMP = ~(-1L << 41L);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /** 允许等待的时钟回拨毫秒数，超过则拒绝生成，避免重复 ID */
    private static final long MAX_BACKWARD_MS = 5L;

    private static final SnowflakeIdUtils INSTANCE = new SnowflakeIdUtils();

    private final long dataCenterId;
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private SnowflakeIdUtils() {
        long[] machineIds = resolveMachineIds();
        this.dataCenterId = machineIds[0];
        this.workerId = machineIds[1];
    }

    /** 生成下一个雪花 ID。 */
    public static long nextId() {
        return INSTANCE.next();
    }

    private synchronized long next() {
        long current = System.currentTimeMillis();
        if (current < lastTimestamp) {
            long backward = lastTimestamp - current;
            if (backward > MAX_BACKWARD_MS) {
                throw new IllegalStateException("检测到时钟回拨 " + backward + "ms，拒绝生成雪花 ID");
            }
            spinWait(lastTimestamp);
            current = System.currentTimeMillis();
            if (current < lastTimestamp) {
                throw new IllegalStateException("等待时钟追上失败，拒绝生成雪花 ID");
            }
        }

        if (current == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0L) {
                current = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = current;
        long delta = current - START_TIMESTAMP;
        if (delta > MAX_TIMESTAMP) {
            throw new IllegalStateException("时间戳超出 41 位上限，请更换 START_TIMESTAMP");
        }
        return (delta << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long last) {
        long current = System.currentTimeMillis();
        while (current <= last) {
            Thread.yield();
            current = System.currentTimeMillis();
        }
        return current;
    }

    private void spinWait(long target) {
        while (System.currentTimeMillis() < target) {
            Thread.yield();
        }
    }

    private static long[] resolveMachineIds() {
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        int at = runtimeName.indexOf('@');
        int pid = 0;
        String host = runtimeName;
        if (at > 0) {
            try {
                pid = Integer.parseInt(runtimeName.substring(0, at));
            } catch (NumberFormatException ignored) {
                pid = 0;
            }
            host = runtimeName.substring(at + 1);
        }

        int hostHash = host.hashCode() & 0x7fffffff;
        long defaultDataCenterId = (hostHash >>> 8) & MAX_DATA_CENTER_ID;
        long defaultWorkerId = (hostHash ^ pid) & MAX_WORKER_ID;

        Long dataCenterId = Long.getLong("snowflake.dataCenterId", defaultDataCenterId);
        Long workerId = Long.getLong("snowflake.workerId", defaultWorkerId);
        if (dataCenterId < 0 || dataCenterId > MAX_DATA_CENTER_ID) {
            throw new IllegalArgumentException("snowflake.dataCenterId 必须在 0~" + MAX_DATA_CENTER_ID + " 之间");
        }
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("snowflake.workerId 必须在 0~" + MAX_WORKER_ID + " 之间");
        }
        return new long[]{dataCenterId, workerId};
    }
}
