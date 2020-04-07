package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
