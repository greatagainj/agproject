package com.atguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * sku图片
 * 
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
@ApiModel
@Data
@TableName("pms_sku_images")
public class SkuImagesEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	@ApiModelProperty(name = "id",value = "id")
	private Long id;
	/**
	 * sku_id
	 */
	@ApiModelProperty(name = "skuId",value = "sku_id")
	private Long skuId;
	/**
	 * 图片地址
	 */
	@ApiModelProperty(name = "imgUrl",value = "图片地址")
	private String imgUrl;
	/**
	 * 排序
	 */
	@ApiModelProperty(name = "imgSort",value = "排序")
	private Integer imgSort;
	/**
	 * 默认图[0 - 不是默认图，1 - 是默认图]
	 */
	@ApiModelProperty(name = "defaultImg",value = "默认图[0 - 不是默认图，1 - 是默认图]")
	private Integer defaultImg;

}
