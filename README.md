# AutoClickPhone

纯手机端 Android 自动点击工具。通过无障碍服务（AccessibilityService）实现对原生 UI App 的自动点击、滑动等操作，无需 root。

- [设计文档](docs/design.md)
- [调试指南](docs/debugging.md)

---

## 本文适合谁

**你刚拿到一台全新的 Windows 电脑，从来没装过任何 Android 开发工具，从 git clone 拉下代码到跑起来，全程跟着做就行。**

不需要你懂 Java、Gradle、SDK 是什么，每一步都写明了点哪里、输什么命令。

---

## 全流程一览

```
装 Android Studio（含 JDK + SDK）
    → git clone 拉代码
    → Studio 打开工程 → Gradle Sync（自动下载依赖）
    → 手机开 USB 调试 → 连电脑
    → 点 Run 安装到手机
    → App 内开权限 → 导入 Demo → 运行
    → 看日志 / 断点调试
```

预计总耗时：**30-60 分钟**（大部分时间在等下载）。

---

## 第 1 步：下载 Android Studio

Android Studio 是 Google 官方 IDE，**一个安装包就包含了 JDK、Android SDK、Gradle**，不需要再单独装任何东西。

1. 打开浏览器，访问：**https://developer.android.com/studio**
2. 页面会自动识别系统，点击 **Download Android Studio**
3. 下载得到一个 `.exe` 文件（约 1.2GB）
4. 如果页面没自动识别系统，手动选 **Windows (64-bit)**

> 这是 Google 官网，安全可放心下载。

---

## 第 2 步：安装 Android Studio

1. 双击下载的 `.exe`
2. **Welcome** → Next
3. **Choose Components** → 保持默认（勾选 Android Studio + Android Virtual Device）→ Next
4. **Install Location** → 保持默认 `C:\Program Files\Android\Android Studio`
   > **不要**改到含中文、空格的路径，否则后面可能出各种怪问题
5. **Install** → 等待安装 → Finish
6. 勾选 **Start Android Studio** → Finish 启动

---

## 第 3 步：首次启动初始化（自动装 SDK）

首次启动会弹设置向导，按以下顺序选：

| 步骤 | 选什么 |
|---|---|
| Import Settings | **Do not import settings** → OK |
| Data Sharing | **Don't send**（随意） |
| Install Type | **Standard** |
| Select UI Theme | 随意选 |
| Verify Settings | 看一眼列表 → Next |
| License Agreement | 每一项点 **Accept** → Finish |

然后开始自动下载 Android SDK（约 2GB）。**等它下完**，时间取决于网速，5-20 分钟。

下完后停在 Welcome 欢迎页，说明环境就绪了。

> 如果卡住下不动，可以挂代理，或跳到本文 [常见问题](#常见问题) 看 SDK 手动装法。

---

## 第 4 步：安装 Git

检查你电脑有没有 Git。按 `Win + R`，输入 `cmd` 回车，然后执行：

```
git --version
```

- 如果显示版本号（如 `git version 2.43.0`）→ 已装，跳到第 5 步
- 如果提示"不是内部或外部命令" → 没装，去下载：

1. 访问 **https://git-scm.com/download/win**
2. 下载 **64-bit Git for Windows Setup**
3. 双击安装，一路 Next 默认即可
4. 装完后**重新打开**命令提示符，再执行 `git --version` 确认

---

## 第 5 步：克隆项目

打开命令提示符，切到你放代码的目录，然后克隆：

```
cd D:\code
git clone <仓库URL> AutoClickPhone
cd AutoClickPhone
```

> 把 `<仓库URL>` 换成实际的 Git 仓库地址。

或者不装 Git，直接在 Android Studio 里克隆（见下一步）。

---

## 第 6 步：用 Android Studio 打开工程

1. Android Studio Welcome 页 → 点 **Open**（如果已经打开了一个项目，菜单栏 `File` → `Open`）
2. 选择 `AutoClickPhone` 文件夹
   > **注意**：选的是**含 `settings.gradle.kts` 的根目录**，不是 `app` 子目录
3. 弹出 **Trust Project?** → 点 **Trust Project**
4. 右下角出现 **Gradle Sync** 进度条

### Gradle Sync 会做什么

首次 Sync 会自动下载：
- Gradle 9.8.2（约 130MB）
- 全部依赖库（Compose、Room、Kotlinx 等约 500MB）

**耐心等 10-20 分钟，期间不要关闭 Android Studio。**

Sync 成功后，底部 Build 面板显示：

```
BUILD SUCCESSFUL
```

项目左侧结构树可以展开，看到 `app/src/main/java/com/autoclick/app/` 下的代码。

### 如果 Sync 失败

| 报错 | 解决 |
|---|---|
| 下载超时 / 网络错误 | 挂代理重试；或见 [常见问题](#常见问题) 配镜像 |
| SDK not found | `File` → `Settings` → `Languages & Frameworks` → `Android SDK` → 勾选 **Android SDK Platform 36** → Apply |
| 提示 JDK 版本 | 确认用的是 Android Studio 自带 JDK，见 [常见问题](#常见问题) |

---

## 第 7 步：准备手机

无障碍功能**必须在真机上验证**，模拟器上可能受限。找一台 Android 手机（Android 7.0 以上）。

### 7.1 开启开发者模式

1. 手机 → **设置** → **关于手机**
2. 找到 **版本号**（有些机型藏在"软件信息"里）
3. 连续点击 **版本号 7 次**
4. 提示"您现在处于开发者模式"

### 7.2 开启 USB 调试

1. 手机 → **设置** → **系统** → **开发者选项**
   > 有些机型在设置最底部，或"其他设置"里
2. 打开 **开发者选项** 总开关
3. 打开 **USB 调试**
4. （如果有）打开 **USB 安装**

### 7.3 连接电脑

1. 用 **数据线**连手机和电脑
   > 有些线只能充电不能传数据，如果连不上换根线试试
2. 手机下拉通知栏 → USB 连接方式选 **文件传输 (MTP)**
3. 手机弹出"是否允许 USB 调试？"→ 勾选**始终允许** → **确定**

### 7.4 验证连接

命令提示符执行：

```
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" devices
```

输出类似这样就对了：

```
List of devices attached
XXXXXXXX    device
```

> 如果显示 `unauthorized`：手机上有没有弹窗没点。
> 如果列表为空：换 USB 线 / 换接口 / 重启 adb（`adb kill-server` 再 `adb devices`）。

---

## 第 8 步：安装到手机并运行

### 8.1 用 Android Studio（推荐）

1. Android Studio 顶部工具栏确认：
   - 左侧下拉框 = **app**
   - 右侧下拉框 = **你的手机型号**
2. 点 **▶ Run**（绿色三角，或按 `Shift + F10`）
3. 等待编译 → 打包 → 安装 → App 自动打开

### 8.2 用命令行

```
cd D:\code\AutoClickPhone
gradlew.bat installDebug
```

安装后手动点手机上的 App 图标打开。

> 产物路径：`app\build\outputs\apk\debug\app-debug.apk`

---

## 第 9 步：App 内授权限

App 打开后先进 **设置** 页（底部导航栏第三个），依次开启三项：

### 9.1 无障碍服务（必须开，否则脚本无法运行）

1. 点"无障碍服务"右侧 **去设置**
2. 跳转到系统无障碍设置页
3. 找到 **AutoClickPhone** → 点开 → 打开开关 → 确认
4. 返回 App，该权限显示"已开启"

### 9.2 悬浮窗权限

1. 点"悬浮窗权限"右侧 **去设置**
2. 找到 **AutoClickPhone** → 打开开关
3. 返回

### 9.3 电池优化白名单

1. 点"电池优化白名单"右侧 **去设置**
2. 选 **不优化** / **允许**
3. 返回

> 三项都"已开启"后才能跑脚本。

---

## 第 10 步：运行 Demo 脚本

1. 进 **脚本** 页（底部导航栏第一个）
2. 点右上角 📂 图标 → 选 **导入 Demo**
3. 列表出现"设置App冒烟测试"
4. 点右侧 **▶ 运行**
5. 手机自动执行：打开系统设置 → 等待 → 返回
6. 进 **日志** 页（底部导航栏第二个）查看运行结果

---

## 第 11 步：调试

### 11.1 看运行日志

**App 内**：底部导航栏 → **日志** 页，每次运行都有记录（成功/失败/耗时/错误信息）。

**Logcat**：Android Studio 底部 → **Logcat** 标签页 → 左侧选设备 → 过滤栏输入 `package:com.autoclick.app` → 看实时日志。

**命令行**：

```
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" logcat -s AutoClickPhone
```

### 11.2 断点调试

1. 在 Android Studio 打开任意 Kotlin 文件（如 `ScriptEngine.kt`）
2. 在想暂停的行号左侧点一下 → 出现 **红点**（断点）
3. 点工具栏 **🐛 Debug** 按钮（不是 ▶ Run），或按 `Shift + F9`
4. 程序运行到断点自动暂停

调试快捷键：

| 快捷键 | 作用 |
|---|---|
| `F8` | 步过（执行当前行，不进函数） |
| `F7` | 步入（进入函数内部） |
| `Shift + F8` | 步出（跳出当前函数） |
| `F9` | 继续运行到下一个断点 |
| `Ctrl + F2` | 停止调试 |

> 更详细的调试说明见 [docs/debugging.md](docs/debugging.md)。

### 11.3 手动测试自定义脚本

新建 `test.json`：

```json
{
  "version": 1,
  "name": "打开设置再返回",
  "targetPackage": "com.android.settings",
  "repeat": 1,
  "steps": [
    { "action": "launchApp", "packageName": "com.android.settings" },
    { "action": "waitFor", "target": { "textContains": "设置" }, "timeout": 10 },
    { "action": "delay", "seconds": 2 },
    { "action": "back" }
  ]
}
```

App → 脚本页 → 📂 → **从文件选择** → 选中该 JSON → 点 ▶ 运行。

---

## 环境要求一览

| 项目 | 版本 | 来源 |
|---|---|---|
| 操作系统 | Windows 10/11 64 位 | — |
| Android Studio | 2026.2 及以上 | 官网下载，含 JDK 25 |
| JDK | 25 | Studio 自带，无需单独装 |
| Android SDK | Platform 36 | Studio 首次启动自动下载 |
| Gradle | 9.8.2 | wrapper 自动下载 |
| Kotlin | 2.2.21 | AGP 内置 |
| 最低运行设备 | Android 7.0 (API 24) | `dispatchGesture` 最低要求 |
| 目标设备 | Android 14 (API 34) | — |

---

## 依赖库

以下库在 Gradle Sync 时自动下载，无需手动操作。完整版本见 [`gradle/libs.versions.toml`](gradle/libs.versions.toml)。

| 库 | 版本 | 用途 |
|---|---|---|
| AndroidX Core KTX | 1.13.1 | Android 核心扩展 |
| AndroidX Lifecycle | 2.8.7 | 生命周期管理 |
| AndroidX Activity Compose | 1.9.3 | Compose Activity 集成 |
| Jetpack Compose BOM | 2024.10.01 | 声明式 UI 框架 |
| Material 3 + Icons Extended | — | Material Design 组件 |
| AndroidX Navigation Compose | 2.8.4 | 页面导航 |
| Room | 2.8.2 | 脚本与运行日志持久化 |
| KSP | 2.3.12 | Room 注解处理 |
| kotlinx.serialization | 1.7.3 | JSON 脚本解析 |
| kotlinx.coroutines | 1.9.0 | 引擎异步执行 |

---

## 常见问题

### Q：Gradle Sync 卡住 / 下载超时

网络问题。两个办法：

**办法 1：挂代理**

`File` → `Settings` → `Appearance & Behavior` → `System Settings` → `HTTP Proxy` → 填代理地址 → Apply → 重新 Sync。

**办法 2：手动下 Gradle 放到对应目录**

1. 浏览器下载：https://services.gradle.org/distributions/gradle-9.8.2-bin.zip
2. 放到 `%USERPROFILE%\.gradle\wrapper\dists\gradle-9.8.2-bin\<hash>\` 目录下
3. 重新 Sync

### Q：提示 SDK not found / compileSdk 36 缺失

`File` → `Settings` → `Languages & Frameworks` → `Android SDK`
- SDK Platforms 标签 → 勾选 **Android SDK Platform 36** → Apply
- SDK Tools 标签 → 确认勾选 **Android SDK Build-Tools** + **Platform-Tools** → Apply

### Q：adb devices 显示不出手机

1. 确认手机 USB 调试已开
2. 换一根**数据线**（有些只能充电）
3. 换一个 USB 口
4. 重启 adb：
   ```
   "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" kill-server
   "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" devices
   ```

### Q：adb 显示 unauthorized

手机上有没有弹"允许调试"的弹窗。拔插 USB 重新连，弹窗出来点允许。

### Q：运行脚本报"无障碍服务未连接"

无障碍权限没开或被系统杀了。去 App 设置页重新开。

### Q：国产手机（小米/华为/OPPO/vivo）无障碍服务经常断

这是国产 ROM 的通病。在手机设置里：
- 锁后台 / 加白名单 / 自启动全开
- 电池优化选"不限制"
- 详细引导见 App 设置页

### Q：编译报 Java 版本错误

确认 Android Studio 用的是自带 JDK：

`File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle`
- **Gradle JDK** 选 **Embedded JDK**（或指向 `C:\Program Files\Android\Android Studio\jbr`）

### Q：点 Run 后 App 闪退

看 Logcat 错误日志：

```
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" logcat *:E
```

找到 `FATAL EXCEPTION` 相关的堆栈，对照排查。

### Q：怎么卸载 App

```
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" uninstall com.autoclick.app
```

或手机长按 App 图标 → 卸载。

---

## 项目结构

```
AutoClickPhone/
├── README.md                     # 本文件
├── docs/
│   ├── design.md                 # 设计文档
│   └── debugging.md             # 调试指南
├── settings.gradle.kts           # Gradle 配置
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml        # 依赖版本目录
│   └── wrapper/                  # Gradle Wrapper
├── gradlew.bat                   # Windows 用
├── gradlew                       # Mac/Linux 用
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── assets/demo_script.json   # 预置 Demo 脚本
        │   ├── java/com/autoclick/app/
        │   │   ├── AutoClickApp.kt       # Application
        │   │   ├── MainActivity.kt       # 单 Activity
        │   │   ├── data/        (9 文件) # Room 持久化
        │   │   ├── engine/      (6 文件) # 脚本引擎
        │   │   ├── locator/     (3 文件) # 元素定位
        │   │   ├── service/     (3 文件) # 无障碍服务
        │   │   ├── keepalive/   (1 文件) # 前台保活
        │   │   └── ui/          (8 文件) # Compose 界面
        │   └── res/             (8 文件) # 资源/图标/配置
        └── test/                (3 文件) # 单元测试
```
