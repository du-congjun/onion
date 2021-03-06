/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.onion.master;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import tachyon.Constants;
import tachyon.client.RemoteBlockReader;
import tachyon.network.protocol.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Read data from from worker data server using Netty.
 */
public final class MasterBlockReader implements RemoteBlockReader {

  private final Bootstrap mClientBootstrap;
  private final ClientHandler mHandler;
  /** A reference to read response so we can explicitly release the resource after reading. */
  private RPCBlockReadResponse mReadResponse = null;

  public MasterBlockReader() {
    mHandler = new ClientHandler();
    mClientBootstrap = NettyClient.createClientBootstrap(mHandler);
  }

  public ByteBuffer readRemoteBlock(InetSocketAddress address, long blockId, long offset,
      long length) throws IOException {

    try {
      ChannelFuture f = mClientBootstrap.connect(address).sync();

      Channel channel = f.channel();
      SingleResponseListener listener = new SingleResponseListener();
      mHandler.addListener(listener);
      channel.writeAndFlush(new RPCBlockReadRequest(blockId, offset, length));

      RPCResponse response = listener.get(NettyClient.TIMEOUT_MS, TimeUnit.MILLISECONDS);
      channel.close().sync();

      switch (response.getType()) {
        case RPC_BLOCK_READ_RESPONSE:
          RPCBlockReadResponse blockResponse = (RPCBlockReadResponse) response;

          RPCResponse.Status status = blockResponse.getStatus();
          if (status == RPCResponse.Status.SUCCESS) {
            // always clear the previous response before reading another one
            close();
            mReadResponse = blockResponse;
            return blockResponse.getPayloadDataBuffer().getReadOnlyByteBuffer();
          }
          throw new IOException(status.getMessage() + " response: " + blockResponse);
        case RPC_ERROR_RESPONSE:
          RPCErrorResponse error = (RPCErrorResponse) response;
          throw new IOException(error.getStatus().getMessage());
        default:
          throw new IOException();
      }
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  /**
   * Release the underlying buffer of previous/current read response.
   */
  public void close() throws IOException {
    if (mReadResponse != null) {
      mReadResponse.getPayloadDataBuffer().release();
      mReadResponse = null;
    }
  }
}
