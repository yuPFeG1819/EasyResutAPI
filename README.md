# EasyResultAPI

Activity Result API的封装库，简化外部调用方式，更符合传统开发流程

参考[Activity Result API的封装思路](https://juejin.cn/post/6987575150283587592)的装饰器封装思路

利用装饰器模式，包装原始`Launcher`对象，开放启动`Intent`时携带对于回调的处理`ActivityResultCallback`。

在`ActivityResultCaller.registerForActivityResult()`方法的回调处执行该回调。

使启动Intent与数据返回更直观，处于相邻代码块便于维护。

## 依赖方式

[![](https://jitpack.io/v/com.gitee.yupfeg/easy-result-api.svg)](https://jitpack.io/#com.gitee.yupfeg/easy-result-api)


```groovy
//root project build.gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

//module build.gradle
dependencies {
    implementation 'com.gitee.yupfeg:easy-result-api:{$lastVersion}'
}

```

## 更新记录

**1.1.0**

1. 新增`Kotlin Coroutine`的支持

2. 优化动态权限请求处理逻辑，并分离提取`kotlin-dsl`配置类的回调函数变量。





## 使用

### 简单使用

在`androidx.activity:activity-ktx:1.2.4`版本依赖后，`ComponentActivity`与`Fragment`都实现了`ActivityResultCaller`。

在视图内直接创建`ActivityResultLauncherWrapper`的子类对象，并在需要使用的地方调用`launch`方法启动Intent与处理返回值

**Tip**：

> `ActivityResultCaller.registerForActivityResult()`方法只能在`onStart`生命周期前执行，所以包装类的初始化也需要在页面创建时进行创建。

### 常用ResultAPI场景封装

- 页面跳转返回 `StartActivityResultLauncher`

  > 同时提供拓展函数，添加了常用的系统页面跳转

- 调起系统相机拍照 `TakePickure`

- 调起系统剪裁：`CropImageLauncher`

- 调起系统内容选择（以 **Intent.ACTION_PICK**方式）`PickContentLauncher`

- 调起系统内容选择（以**Intent.ACTION_GET_CONTENT**方式）`GetContentLauncher`、`GetMultiContentLauncher`

- 调起系统拨号页面（无返回值） `CallPhoneLauncher`

- 权限请求（多权限） `RequestPermissionLauncher`

  > - 由于**Fragment实现了ActivityResultCaller**，所以**不需要利用不可见的Fragment**来间接实现的方式，也可直接在Fragment内请求权限
  >
  > - 提供了kotlin dsl方式的请求权限配置`RequestPermissionConfig`
  > - 提供默认实现的解释申请权限原因的说明弹窗


### 更多使用场景

如果上述封装都不能满足使用，则可继承`ActivityResultLauncherWrapper`，实现`ActivityResultContract`接口定义Intent与返回值处理。

> 官方已经提供了很多现成的协议类 `ActivityResultContracts`

## TODO

- ~~添加**kotlin coroutines**的拓展，进一步简化回调处理方式~~（1.1.0已更新）

- 添加更多常用ResultAPI使用场景封装

## Thanks

[Activity Result API的封装思路](https://juejin.cn/post/6987575150283587592)

[PermissionX](https://github.com/guolindev/PermissionX)