package org.apache.dubbo.demo.cloud.consumer.interceptor;

import com.alibaba.cloud.dubbo.registry.DubboCloudRegistry;
import com.alibaba.cloud.dubbo.registry.event.ServiceInstancesChangedEvent;
import com.alibaba.fastjson.JSON;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.registry.ListenerRegistryWrapper;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.integration.DynamicDirectory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.interceptor.ClusterInterceptor;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: cn.ygyg.account.application.dubbo.NoProviderClusterInterceptor
 * Description:
 *
 * @Author: liuhongtao
 * Date: 2022/11/11 15:33
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
@Activate
public class NoProviderClusterInterceptor implements ClusterInterceptor, ClusterInterceptor.Listener {

    private static final Logger log = LoggerFactory.getLogger(NoProviderClusterInterceptor.class);

    @Override
    public void onMessage(Result appResponse, AbstractClusterInvoker<?> clusterInvoker, Invocation invocation) {

    }

    @Override
    public void onError(Throwable t, AbstractClusterInvoker<?> clusterInvoker, Invocation invocation) {
        if (t instanceof RpcException && ((RpcException) t).getCode() == RpcException.FORBIDDEN_EXCEPTION) {
            final String remoteService = clusterInvoker.getUrl().getParameter(CommonConstants.REMOTE_APPLICATION_KEY);
            // 刷新当前服务  可用 提供者 信息
            if (!StringUtils.isBlank(remoteService)) {
                reSubscribe(clusterInvoker, remoteService);
            } else {
                log.info("ServiceRevisionInstanceMap:{}",
                        JSON.toJSON(((DubboCloudRegistry) ((ListenerRegistryWrapper) ((DynamicDirectory) clusterInvoker.getDirectory()).getRegistry()).getRegistry()).getServiceRevisionInstanceMap()));
            }
        }
    }

    private void reSubscribe(AbstractClusterInvoker<?> clusterInvoker, String remoteService) {
        log.info("Refresh subscribedService {} when forbidden", remoteService);
        List<ServiceInstance> serviceInstances = BeanHolder.getDiscoveryClient().getInstances(remoteService);
        ServiceInstancesChangedEvent changedEvent = new ServiceInstancesChangedEvent(remoteService,
                serviceInstances);
        DynamicDirectory<?> directory = (DynamicDirectory<?>) clusterInvoker.getDirectory();
        final Registry registry = directory.getRegistry();
        if (registry instanceof ListenerRegistryWrapper) {
            final Registry registryInstance = ((ListenerRegistryWrapper) registry).getRegistry();
            if (registryInstance instanceof DubboCloudRegistry) {
                final DubboCloudRegistry dubboCloudRegistry = (DubboCloudRegistry) registryInstance;
                dubboCloudRegistry.getServiceRevisionInstanceMap().put(remoteService, new HashMap<>());
                dubboCloudRegistry.onApplicationEvent(changedEvent);
            }
        }
    }

    @Override
    public void before(AbstractClusterInvoker<?> clusterInvoker, Invocation invocation) {
        log.info("url parameter:{}, invocation parameter:{}", JSON.toJSON(clusterInvoker.getUrl().getParameters()),
                JSON.toJSON(invocation.getAttachments()));
        if (!clusterInvoker.getDirectory().isAvailable()) {
            DynamicDirectory<?> directory = (DynamicDirectory<?>) clusterInvoker.getDirectory();
            final Registry registry = directory.getRegistry();
            if (registry instanceof ListenerRegistryWrapper) {
                final Registry registryInstance = ((ListenerRegistryWrapper) registry).getRegistry();
                if (registryInstance instanceof DubboCloudRegistry) {
                    final Map<String, Map<String, List<ServiceInstance>>> serviceRevisionInstanceMap =
                            ((DubboCloudRegistry) registryInstance).getServiceRevisionInstanceMap();
                    final Set<String> serviceSet = serviceRevisionInstanceMap.keySet();
                    for (String application : serviceSet) {
                        final Map<String, List<ServiceInstance>> revisionInstanceMap =
                                serviceRevisionInstanceMap.get(application);
                        if (CollectionUtils.isEmpty(revisionInstanceMap) || revisionInstanceMap.containsKey("0")) {
                            reSubscribe(clusterInvoker, application);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void after(AbstractClusterInvoker<?> clusterInvoker, Invocation invocation) {

    }

}
