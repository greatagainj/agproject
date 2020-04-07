package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
