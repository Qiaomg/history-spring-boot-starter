package net.shopin.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.history.GreeterGrpc;
import io.grpc.history.RequestOperateSql;
import io.grpc.history.ResponseOperateSql;
import net.shopin.history.interceptors.HistoryTableInterceptor;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @title: GRpcClient
 * @description: gRpc client 客户端
 * @author: qmg
 * @date: 2020/6/10 11:29
 * @version: V1.0
 */
public class GRpcClient {
    private static final Logger logger = Logger.getLogger(GRpcClient.class.getName());
    private static ManagedChannel channel = null;
    private static GreeterGrpc.GreeterBlockingStub blockingStub =null;

    private volatile static GRpcClient client;
    private GRpcClient(){}
    public GRpcClient(String host, int port){
        channel = ManagedChannelBuilder.forAddress(host,port)
                .usePlaintext(true)
                .build();

        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    /**
     * 单例 双检锁模式
     * @return HistoryTableInterceptor
     */
    public static GRpcClient getInstance(){
        if(client == null){
            synchronized (HistoryTableInterceptor.class){
                if(client == null){
                    System.out.println("init HistoryTableInterceptor");
                    client = new GRpcClient("127.0.0.1",50051);
                }
            }
        }
        return client;
    }

    /**
     * 关闭连接
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * 发送数据
     * @param name 数据包内容 格式：xxx
     */
    public void greet(String name){
        RequestOperateSql request = RequestOperateSql.newBuilder().setName(name).build();
        ResponseOperateSql response;
        try{
            response = blockingStub.sendOptSql(request);
        } catch (StatusRuntimeException e)
        {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("rpc server: "+response.getMessage());
    }
}
