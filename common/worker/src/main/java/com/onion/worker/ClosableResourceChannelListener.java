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

package com.onion.worker;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.Closeable;

/**
 * A listener that will close the given resource when the operation completes. This class accepts
 * null resources.
 */
final class ClosableResourceChannelListener implements ChannelFutureListener {
  private final Closeable mResource;

  ClosableResourceChannelListener(Closeable resource) {
    mResource = resource;
  }


  public void operationComplete(ChannelFuture future) throws Exception {
    if (mResource != null) {
      mResource.close();
    }
  }
}
