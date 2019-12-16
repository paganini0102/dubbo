/*
 * Copyright 1999-2011 Alibaba Group.
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
package com.alibaba.dubbo.rpc.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcStatus;

/**
 * LimitInvokerFilter
 *
 * @author william.liangf
 */
@Activate(group = Constants.CONSUMER, value = Constants.ACTIVES_KEY)
public class ActiveLimitFilter implements Filter {

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取URL和调用的方法名称
        URL url = invoker.getUrl();
        String methodName = invocation.getMethodName();
        // 获取设置的actives的值（默认为0）和最大可用并发数
        int max = invoker.getUrl().getMethodParameter(methodName, Constants.ACTIVES_KEY, 0);
        // 根据URL和方法名获取对应的状态对象
        RpcStatus count = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName());
        // 判断是不是超过并发限制
        if (max > 0) {
            long timeout = invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.TIMEOUT_KEY, 0);
            long start = System.currentTimeMillis();
            long remain = timeout;
            int active = count.getActive();
            if (active >= max) {
                // 超过并发限制则阻塞当前线程timeout时间
                synchronized (count) {
                    while ((active = count.getActive()) >= max) {
                        try {
                            count.wait(remain);
                        } catch (InterruptedException e) {
                        }
                        long elapsed = System.currentTimeMillis() - start;
                        remain = timeout - elapsed;
                        // 超时了还没被唤醒则抛出异常
                        if (remain <= 0) {
                            throw new RpcException("Waiting concurrent invoke timeout in client-side for service:  "
                                    + invoker.getInterface().getName() + ", method: "
                                    + invocation.getMethodName() + ", elapsed: " + elapsed
                                    + ", timeout: " + timeout + ". concurrent invokes: " + active
                                    + ". max concurrent invoke limit: " + max);
                        }
                    }
                }
            }
        }
        try {
            // 说明激活并发数没达到限制，则继续Filter链的调用，正常发起远程调用
            long begin = System.currentTimeMillis();
            RpcStatus.beginCount(url, methodName);
            try {
                Result result = invoker.invoke(invocation);
                // 远程调用完成后，当前激活并发数减去1，并通过notifyAll()方法激活所有挂起线程
                RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, true);
                return result;
            } catch (RuntimeException t) {
                RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, false);
                throw t;
            }
        } finally {
            if (max > 0) {
                synchronized (count) {
                    count.notify();
                }
            }
        }
    }

}