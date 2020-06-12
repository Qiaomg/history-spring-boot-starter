package net.shopin.grpc;

/**
 * @title: GrpcTypeEnum
 * @description: Server enum
 * @author: qmg
 * @date: 2020/6/12 9:57
 * @version: V1.0
 */
public enum GrpcTypeEnum {
    /**
     * 成功状态
     */
    OK,
    /**
     * 建表
     */
    CREATE,
    /**
     * 增加数据
     */
    INSERT,
    /**
     * 异常
     */
    FAIL;
}
