# gmall
Guli Mall

新增购物车：POST
localhost:8090/cart
{
    "skuId": 20,
    "count": 2
}

登录认证：POST
localhost:8089/auth/accredit
username lisi
password 1244556

查询购物车：GET
localhost:8089/cart

更新购物车：POST
localhost:8090/cart/update

删除购物车（单个）：POST
localhost:8090/cart/delete/11

生成订单：localhost:8092/order/confirm

提交订单：localhost:8092/order/submit
{
    "orderToken": "1246031730704424961",
    "address": {
        "id": 1,
        "memberId": 3,
        "name": "张三",
        "phone": "16666666666",
        "postCode": "266600",
        "province": "山东省",
        "city": "青岛市",
        "region": "莱西市",
        "detailAddress": "水集街道李家疃",
        "areacode": "532",
        "defaultStatus": 1
    },
    "payType": "1",
    "deliveryCompany": "顺丰速运",
    "orderItems": [
        {
            "skuId": 8,
            "title": "锤子坚果pro3 8g,黑色",
            "defaultImage": "https://gmallz.oss-cn-beijing.aliyuncs.com/2020-03-17/4b46a744-23c4-4c72-8735-7f9db30ad5cf_chuizi2.jpg",
            "price": 99999,
            "count": 20,
            "weight": 123,
            "store": true,
            "saleAttrValues": [
                {
                    "id": 13,
                    "skuId": 8,
                    "attrId": 24,
                    "attrName": null,
                    "attrValue": "8g",
                    "attrSort": null
                },
                {
                    "id": 14,
                    "skuId": 8,
                    "attrId": 30,
                    "attrName": null,
                    "attrValue": "黑色",
                    "attrSort": null
                }
            ],
            "sales": null
        }
    ],
    "bounds": "200",
    "totalPrice": "1999980"
}