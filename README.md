# zeus-logger
一个记录操作日志的框架，通过注解非常方便的记录目标对象在方法执行前后关注的值变化

#### 技术栈
* Spring Boot 2.5.2
* JDK 11
* disruptor 3.4.4

## 注解

### @Logger

| 属性名称             | 数值类型 | 默认值                    | 描述                                                         |
| -------------------- | -------- | ------------------------- | ------------------------------------------------------------ |
| topic                | String   | ""                        | 日志主题                                                     |
| paramIndex           | int      | 0                         | 方法参数索引，selectParam的spel表达式将作用于该对象          |
| selectParam          | String   | #root                     | 该值为spel表达式，用于获取需要记录日志的对象。例：单个对象参数 #root.id、集合参数 #root?.peopleList?.![id] |
| selectMethod         | String   | ""                        | 该值为spel表达式，用于获取目标对象在方法执行前后的状态。例：@beanName.get(#root)，其中#root表示selectParam的执行结果 |
| customExtData        | Class[]  | {}                        | 自定义扩展数据的class对象数组，用于获取除目标对象外的其他扩展信息，需要实现 `com.jz.logger.core.LoggerExtensionData` 接口 |
| disableGlobalExtData | boolean  | false                     | 禁用全局扩展数据。zeus-logger 支持通过在 `application.yaml` 中配置  `zeus.logger.globalExtensionDatas` 属性定义全局扩展数据。 |
| handlerBeanName      | String   | defaultLoggerTraceHandler | 指标处理器的bean name                                        |
| strategy             | Strategy | Strategy.ASYN_SERIAL      | 指标处理策略                                                 |

###  @Trace

| 属性名称    | 数值类型 | 默认值 | 描述                                                         |
| ----------- | -------- | ------ | ------------------------------------------------------------ |
| tag         | String   | ""     | 标签，相当于指标的名称                                       |
| targetValue | String   | ""     | 该值为spel表达式，当指标为引用类型对象时，可通过该表达式获取指定属性的值，在记录指标值变化时将直接作用于该值 |
| topic       | String[] | {}     | 当指定了topic后，将仅在指定的topic下记录该指标的变化，若没有指定则在所有topic下都会记录 |
| order       | int      | 0      | 指标顺序                                                     |

## 配置

```yaml
zeus:
  logger:
    concurrentNum: 4                 # 异步并发数量，默认为CPU数量
    concurrentRingBufferSize: 1024   # 异步并发策略下队列长度，默认1024
    serialRingBufferSize: 1024       # 异步串行策略下队列长度，默认1024
    globalExtensionDatas: com.jz.logger.demo.CustomExtensionDatas  # 全局扩展数据class，需要定义为bean
```

**备注：**

> 扩展数据对象需要定义为bean

## 指标处理器(LoggerTraceHandler)

**zeus-logger** 默认 `LoggerTraceHandler` 实现是将指标的变化通过 log 打印出来，用户可通过实现该接口自定义指标处理方案。

#### 1.LoggerTraceHandler源码

```java
public interface LoggerTraceHandler {

    void execute(LoggerInfo loggerInfo);

}
```

#### 2.自定义默认指标处理器

```java
@Configuration
public class CustomConfiguration {

    @Bean("defaultLoggerTraceHandler")
    public LoggerTraceHandler loggerTraceHandler() {
        return new CustomTraceHandler();
    }

}
```

#### 3.自定义指标处理器

通过 `@Logger` 注解的 `handlerBeanName` 属性即可针对不同 `@Logger` 使用不同的 `LoggerTraceHandler`。

## 快速开始

具体使用可参考 **demo** 模块，代码可直接运行，这里提供一个请求参数

```json
{
    "family": {
        "id": 1,
        "name": "修改名称",
        "host": {
            "id": 66,
            "name": "赵四"
        }
    },
    "familyList": [
        {
            "id": 1,
            "name": "张家改名"
        },
        {
            "id": 2,
            "name": "王家改名"
        }
    ]
}
```