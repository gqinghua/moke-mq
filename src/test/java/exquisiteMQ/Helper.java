package exquisiteMQ;

import com.moke.client.MetaClientConfig;
import com.moke.commons.utils.ZkUtils;

/**
 * @author jianglinzou
 * @date 2019/3/18 下午5:30
 */
public class Helper {

    public static MetaClientConfig initMetaConfig(){

        final MetaClientConfig metaClientConfig = new MetaClientConfig();
        final ZkUtils.ZKConfig zkConfig = new ZkUtils.ZKConfig();
        zkConfig.zkConnect = "123.57.128.134:2181";
        zkConfig.zkRoot = "/";
        metaClientConfig.setZkConfig(zkConfig);
        return metaClientConfig;

    }
}
