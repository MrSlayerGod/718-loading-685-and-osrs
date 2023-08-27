package com.rs.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import com.rs.Settings;
import com.rs.io.InputStream;
import com.rs.net.decoders.WorldPacketsDecoder;
import com.rs.utils.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import vpn.detection.Response;
import vpn.detection.VPNDetection;

public final class GameChannelsManager extends ByteToMessageDecoder {

	private static ChannelGroup channels;
	private static ServerBootstrap bootstrap;
	private static EventLoopGroup workerExecutor, bossExecutor;

	private Session session;
	
	public static final void init() {
		channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		workerExecutor = new NioEventLoopGroup(2);//Executors.newFixedThreadPool(2);
		bossExecutor = new NioEventLoopGroup(1);//Executors.newSingleThreadExecutor();
		bootstrap = new ServerBootstrap().group(bossExecutor, workerExecutor).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new IdleStateHandler(Settings.READ_TIMEOUT, Settings.WRITE_TIMEOUT, 0));
				ch.pipeline().addLast(new ChannelTrafficShapingHandler(Settings.WORLD_WRITE_RATE, Settings.WORLD_READ_RATE));	
				ch.pipeline().addLast(new GameChannelsManager());
			}
			
		});
				
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);		
		
		
		bootstrap.bind(new InetSocketAddress(Settings.GAME_ADDRESS_BASE.getAddress(), Settings.GAME_ADDRESS_BASE.getPort() + Settings.WORLD_ID));
	}
	
	
	public static int getChannelCount(Channel compare) {
		String ip = ((InetSocketAddress)compare.remoteAddress()).getAddress().getHostAddress();
		int count = 0;
		for(Channel connection : channels) {
			String ip2 = ((InetSocketAddress)connection.remoteAddress()).getAddress().getHostAddress();
			if(ip2.equals(ip))
				count++;
				
		}
		return count;
	}


	public static final void shutdown() {
		channels.close().awaitUninterruptibly();
		workerExecutor.shutdownGracefully();
		bossExecutor.shutdownGracefully();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (Settings.HOSTED && getChannelCount(ctx.channel()) > Settings.CONNECTIONS_LIMIT) {
			ctx.channel().close();
			return;
		}
		session = new Session(ctx.channel());
		channels.add(ctx.channel());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (session != null && session.getDecoder() != null) {
		    if (session.getDecoder() instanceof WorldPacketsDecoder)
		    	session.getWorldPackets().getPlayer().finish();
		}
		ctx.channel().close();
		channels.remove(ctx.channel());
		super.channelInactive(ctx);
	}
	
	//make exception remove
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	if (session != null && session.getDecoder() != null) {
		    if (session.getDecoder() instanceof WorldPacketsDecoder)
		    	session.getWorldPackets().getPlayer().finish();
		}
		ctx.channel().close();
		channels.remove(ctx.channel());
    }
    
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (session == null || session.getChannel() == null || !session.getChannel().isActive() || session.getDecoder() == null || in.readableBytes() < 1)
			return;
		int readerIndex = in.readerIndex();
		byte[] buffer = new byte[in.readableBytes()];
		in.readBytes(buffer);
		try {
			int read = session.getDecoder().decode(new InputStream(buffer));
			if (read != -1) 
				in.readerIndex(readerIndex + read);
		} catch (Throwable er) {
			Logger.handle(er);
		}
	}

}
