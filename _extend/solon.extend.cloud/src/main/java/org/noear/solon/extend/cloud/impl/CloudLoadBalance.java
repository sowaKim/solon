package org.noear.solon.extend.cloud.impl;

import org.noear.solon.Utils;
import org.noear.solon.core.LoadBalance;
import org.noear.solon.extend.cloud.CloudManager;
import org.noear.solon.extend.cloud.model.Discovery;
import org.noear.solon.extend.cloud.model.Node;

/**
 * 负载均衡
 *
 * @author noear
 * @since 1.2
 */
public class CloudLoadBalance implements LoadBalance {
    private String service;
    private Discovery discovery;
    private int index = 0;
    private static int indexMax = 99999999;

    public CloudLoadBalance(String service) {
        this.service = service;
        CloudManager.registerService().attention(service, d1 -> {
            this.discovery = d1;
        });
    }

    @Override
    public String getServer() {
        if (discovery == null) {
            return null;
        } else {
            int count = discovery.cluster.size();
            if (count == 0) {
                return null;
            } else {
                //这里不需要原子性
                if (index > indexMax) {
                    index = 0;
                }
                Node node = discovery.cluster.get(index++ % count);

                if (Utils.isEmpty(node.protocol)) {
                    return "http://" + node.ip + ":" + node.port;
                } else {
                    return node.protocol + "://" + node.ip + ":" + node.port;
                }
            }
        }
    }
}
