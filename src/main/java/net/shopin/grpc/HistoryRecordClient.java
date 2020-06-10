package net.shopin.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.history.GreeterGrpc;
import io.grpc.history.RequestOperateSql;
import io.grpc.history.ResponseOperateSql;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @title: HistoryRecordClient
 * @description: TODO(用一句话描述该文件做什么)
 * @author: qmg
 * @date: 2020/6/10 11:29
 * @version: V1.0
 */
public class HistoryRecordClient {

    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private static final Logger logger = Logger.getLogger(HistoryRecordClient.class.getName());

    public HistoryRecordClient(String host, int port){
        channel = ManagedChannelBuilder.forAddress(host,port)
                .usePlaintext(true)
                .build();

        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }


    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public  void greet(String name){
        RequestOperateSql request = RequestOperateSql.newBuilder().setName(name).build();
        ResponseOperateSql response;
        try{
            response = blockingStub.sendOptSql(request);
        } catch (StatusRuntimeException e)
        {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Message from gRPC-Server: "+response.getMessage());
    }

    public static void main(String[] args) throws InterruptedException {
        HistoryRecordClient client = new HistoryRecordClient("127.0.0.1",50051);
        try{
            String user = "world";
            if (args.length > 0){
                user = args[0];
            }
            for (int i = 0; i <100 ; i++) {
                client.greet(user + ":" + i);
            }

        }finally {
            client.shutdown();
        }
    }
}
