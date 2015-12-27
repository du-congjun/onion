/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.onion.master;

import com.onion.eclib.ECHandler;
import com.onion.eclib.ErasureCoder;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Onion master to handle storage affairs.
 */
public class Master {
    //Map input file to storage node file.
    private Map<String, List<Integer>> storageMap = new HashMap<String, List<Integer>>();
    private MasterConf masterConf;
    private List<InetSocketAddress> workerAddresses;
    private int dataWorkerAmount;
    private int parityWorkerAmount;
    private int wordSize;
    private int packetSize;
    private ErasureCoder coder;
    private ECHandler ecHandler;

    public Master(File confDir) throws Exception {
        masterConf = new MasterConf(confDir);
        workerAddresses = masterConf.getWorkerAddresses();
        dataWorkerAmount = masterConf.getDataWorkerAmount();
        parityWorkerAmount = masterConf.getParityWorkerAmount();
        wordSize = masterConf.getWordSize();
        packetSize = masterConf.getPacketSize();
        Class coderClass = Class.forName("com.onion.eclib." + masterConf.getErasureCodeType());
        Constructor<?> constructor = coderClass.getConstructor(int.class,
                int.class, int.class, int.class);
        coder = (ErasureCoder) constructor.newInstance(dataWorkerAmount, parityWorkerAmount,
                wordSize, packetSize);
//        coder = new CauchyGoodRSCoder(dataWorkerAmount,
//                parityWorkerAmount, wordSize, packetSize);
        //Support Cauchcy Good RS code now, more coding types will be imported.
        ecHandler = new ECHandler(dataWorkerAmount,
                parityWorkerAmount, coder, wordSize, packetSize);
    }

    public boolean write(String srcPath) throws Exception{
        //TODO
        byte[][] encodeData = ecHandler.encode(srcPath);
        List<InetSocketAddress> addresses = masterConf.getWorkerAddresses();

        return false;
    }

    public boolean read(String inputFile, String recoveredFile) {
        //TODO
        return false;
    }

    public boolean delete(File inputFile) {
        //TODO
        return false;
    }

    private synchronized long generateBlockId() {
        return 0;
    }

}
