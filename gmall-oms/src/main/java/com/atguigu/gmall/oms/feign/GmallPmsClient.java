package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
