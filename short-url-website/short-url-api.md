# 短链接服务 API 文档

## 基础信息

- 基础路径: `/api/shorturls`
- 响应格式: JSON

## 通用响应格式

### 基础响应结构

```json
{
    "code": 0,       // 响应码：0-成功，1-失败
    "message": "",  // 响应消息
    "data": null,    // 响应数据
    "requestId": "" // 请求ID
}
```

### 分页响应结构

```json
{
    "code": 0,          // 响应码：0-成功，1-失败
    "message": "",     // 响应消息
    "data": null,       // 响应数据
    "requestId": "",   // 请求ID
    "total": 0,        // 总记录数
    "pageSize": 10,    // 每页大小
    "current": 1,      // 当前页码
    "pages": 0         // 总页数
}
```

## 接口列表

### 1. 创建短链接

#### 请求信息

- 接口路径：`/api/shorturls/shorten`
- 请求方法：`POST`
- Content-Type：`application/json`

#### 请求参数

```json
{
    "longUrl": "https://example.com/very/long/url",  // 原始长链接（必填）
    "alias": "custom-code",                        // 自定义短码（可选）
    "expirationDate": "2024-12-31T23:59:59"       // 过期时间（可选）
}
```

#### 响应结果

```json
{
    "code": 0,
    "message": "success",
    "data": "http://short.domain/abc123",
    "requestId": "xxx"
}
```

### 2. 短链接跳转

#### 请求信息

- 接口路径：`/api/shorturls/{shortCode}`
- 请求方法：`GET`

#### 路径参数

- `shortCode`: 短链接编码

#### 响应结果

- 成功：HTTP 301 重定向到原始URL
- 失败：HTTP 404 Not Found

### 3. 查询短链接列表

#### 请求信息

- 接口路径：`/api/shorturls/list`
- 请求方法：`POST`
- Content-Type：`application/json`

#### 请求参数

```json
{
    "pageNum": 1,                    // 页码（默认1）
    "pageSize": 10,                 // 每页大小（默认10）
    "shortCode": "abc",            // 短码关键字（可选）
    "longUrl": "example.com"       // 长链接关键字（可选）
}
```

#### 响应结果

```json
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "shortCode": "abc123",
            "longUrl": "https://example.com/path",
            "createdAt": "2023-01-01T12:00:00",
            "expiresAt": "2024-01-01T12:00:00",
            "clickCount": 100,
            "createTime": "2023-01-01T12:00:00"
        }
    ],
    "requestId": "xxx",
    "total": 100,
    "pageSize": 10,
    "current": 1,
    "pages": 10
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 1 | 失败 |

## 注意事项

1. 所有请求都需要确保Content-Type设置正确
2. 分页查询的pageNum和pageSize参数都有默认值，可以不传
3. 短链接一旦创建成功后无法修改
4. 过期的短链接将无法访问，返回404错误