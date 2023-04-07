package com.cleanroommc.modularui.mixins;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.INetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Method;

@Mixin(SimpleNetworkWrapper.class)
public abstract class SimpleNetworkWrapperMixin {

    private static final Class<?> defaultChannelPipeline;
    private static final Method generateName;

    static {
        try {
            defaultChannelPipeline = Class.forName("io.netty.channel.DefaultChannelPipeline");
            generateName = defaultChannelPipeline.getDeclaredMethod("generateName", ChannelHandler.class);
            generateName.setAccessible(true);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Shadow(remap = false)
    protected abstract <REPLY extends IMessage, REQ extends IMessage> SimpleChannelHandlerWrapper<REQ, REPLY> getHandlerWrapper(IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Side side, Class<REQ> requestType);

    /**
     * @author miozune
     * @reason Allow registering more than one handler of the same type. Backported from 1.12
     */
    @Overwrite(remap = false)
    private <REQ extends IMessage, REPLY extends IMessage, NH extends INetHandler> void addServerHandlerAfter(FMLEmbeddedChannel channel, String type, IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Class<REQ> requestType) {
        SimpleChannelHandlerWrapper<REQ, REPLY> handler = getHandlerWrapper(messageHandler, Side.SERVER, requestType);
        channel.pipeline().addAfter(type, generateName(channel.pipeline(), handler), handler);
    }

    /**
     * @author miozune
     * @reason Allow registering more than one handler of the same type. Backported from 1.12
     */
    @Overwrite(remap = false)
    private <REQ extends IMessage, REPLY extends IMessage, NH extends INetHandler> void addClientHandlerAfter(FMLEmbeddedChannel channel, String type, IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Class<REQ> requestType) {
        SimpleChannelHandlerWrapper<REQ, REPLY> handler = getHandlerWrapper(messageHandler, Side.CLIENT, requestType);
        channel.pipeline().addAfter(type, generateName(channel.pipeline(), handler), handler);
    }

    private String generateName(ChannelPipeline pipeline, ChannelHandler handler) {
        try {
            return (String)generateName.invoke(defaultChannelPipeline.cast(pipeline), handler);
        }
        catch (Exception e) {
            throw new RuntimeException("It appears we somehow have a not-standard pipeline. Huh", e);
        }
    }
}
