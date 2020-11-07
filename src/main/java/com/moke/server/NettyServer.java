package com.moke.server;

import com.moke.commons.utils.codec.SerializerFactory;
import com.moke.model.MokeRequest;
import com.moke.model.MokeResponse;
import com.moke.server.ProducerMessageListener.ProducerMessageListener;
import com.moke.server.encode.ServerNettyMessageDecoder;
import com.moke.server.encode.ServerNettyMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author jianglinzou
 * @date 2019/3/11 下午4:10
 */
public class NettyServer implements  BrokerServer{

    private EventLoopGroup bossGroup;
    private ServerBootstrap bootstrap;
    private EventLoopGroup workerGroup;

    public static Logger logger = LoggerFactory.getLogger(NettyServer.class);



    @Override
    public void init() {
        final BrokerHandler handler = new BrokerHandler();

        //设置两个监听器
        handler.setProducerListener(new ProducerMessageListener());
        handler.setConsumerRequestListener(new ConsumerMessageListener());

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //处理事件的线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup(30);
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new RpcEncoder(MokeResponse.class));
                            socketChannel.pipeline().addLast(new RpcDecoder(MokeRequest.class));
                            socketChannel.pipeline().addLast(handler);
                        }
                    }).option(ChannelOption.SO_KEEPALIVE,true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void start(int port) throws Exception {
        // 配置NIO线程组
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            // 服务器辅助启动类配置

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChildChannelHandler())//
                    .option(ChannelOption.SO_BACKLOG, 1024) // 设置tcp缓冲区 // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            // 绑定端口 同步等待绑定成功
            ChannelFuture f = bootstrap.bind(port).sync(); // (7)
            // 等到服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅释放线程资源
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    public void close() {
        if (!Objects.isNull(workerGroup)) {
            workerGroup.shutdownGracefully();
        }
        if (Objects.nonNull(bossGroup)) {
            bossGroup.shutdownGracefully();
        }


    }

    /**
     * 网络事件处理器
     */
    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            // 添加自定义协议的编解码工具
            ch.pipeline().addLast(new ServerNettyMessageDecoder(SerializerFactory.buildSerializer(SerializerFactory.Codec_Type.HESSIAN)));
            ch.pipeline().addLast(new ServerNettyMessageEncoder(SerializerFactory.buildSerializer(SerializerFactory.Codec_Type.HESSIAN)));
            // 处理网络IO
            ch.pipeline().addLast(new DefaultServerNettyHandler());
        }
    }

//    public static void main(String[] args) throws Exception {
//        new NettyServer().bind(9999);
//    }
}
