/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.naming.consistency.ephemeral.distro.v2;

import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.cluster.remote.ClusterRpcClientProxy;
import com.alibaba.nacos.core.distributed.distro.DistroProtocol;
import com.alibaba.nacos.core.distributed.distro.component.DistroComponentHolder;
import com.alibaba.nacos.core.distributed.distro.component.DistroTransportAgent;
import com.alibaba.nacos.core.distributed.distro.task.DistroTaskEngineHolder;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManager;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManagerDelegate;
import com.alibaba.nacos.naming.misc.GlobalConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Distro component registry for v2.
 *
 * @author xiweng.yy
 */
@Component
public class DistroClientComponentRegistry {

    // Nacos节点管理器
    private final ServerMemberManager serverMemberManager;

    // Distro协议对象
    private final DistroProtocol distroProtocol;

    // 一些Distro组件的集合
    private final DistroComponentHolder componentHolder;

    // 一些任务执行引擎的集合
    private final DistroTaskEngineHolder taskEngineHolder;

    //存储发行版协议的一些配置。
    private final GlobalConfig globalConfig;

    // Nacos 客户端管理器
    private final ClientManager clientManager;

    // Nacos 客户端管理器
    private final ClusterRpcClientProxy clusterRpcClientProxy;

    public DistroClientComponentRegistry(ServerMemberManager serverMemberManager, DistroProtocol distroProtocol,
            DistroComponentHolder componentHolder, DistroTaskEngineHolder taskEngineHolder, GlobalConfig globalConfig,
            ClientManagerDelegate clientManager, ClusterRpcClientProxy clusterRpcClientProxy) {
        this.serverMemberManager = serverMemberManager;
        this.distroProtocol = distroProtocol;
        this.componentHolder = componentHolder;
        this.taskEngineHolder = taskEngineHolder;
        this.globalConfig = globalConfig;
        this.clientManager = clientManager;
        this.clusterRpcClientProxy = clusterRpcClientProxy;
    }

    /**
     * Register necessary component to distro protocol for v2 {@link com.alibaba.nacos.naming.core.v2.client.Client}
     * implement.
     */
    @PostConstruct
    public void doRegister() {
        DistroClientDataProcessor dataProcessor = new DistroClientDataProcessor(clientManager, distroProtocol);
        DistroTransportAgent transportAgent = new DistroClientTransportAgent(clusterRpcClientProxy,
                serverMemberManager);
        DistroClientTaskFailedHandler taskFailedHandler = new DistroClientTaskFailedHandler(globalConfig,
                taskEngineHolder);
        // 注册Nacos:Naming:v2:ClientData类型数据的数据仓库实现
        componentHolder.registerDataStorage(DistroClientDataProcessor.TYPE, dataProcessor);
        // 注册Nacos:Naming:v2:ClientData类型的DistroData数据处理器
        componentHolder.registerDataProcessor(dataProcessor);
        // 注册Nacos:Naming:v2:ClientData类型数据的数据传输代理对象实现
        componentHolder.registerTransportAgent(DistroClientDataProcessor.TYPE, transportAgent);
        // 注册Nacos:Naming:v2:ClientData类型的失败任务处理器
        componentHolder.registerFailedTaskHandler(DistroClientDataProcessor.TYPE, taskFailedHandler);
    }
}
