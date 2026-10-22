# AutoClickPhone

纯手机端 Android 自动点击工具。通过无障碍服务（AccessibilityService）实现对原生 UI App 的自动点击、滑动等操作，无需 root。

详见 [设计文档](docs/design.md)。

---

## 从零开始（Windows）

以下假设你的电脑上**什么开发工具都没装**。按顺序做完就能跑起来。

### 第 1 步：下载 Android Studio

1. 用浏览器打开官方下载页：https://developer.android.com/studio
2. 页面会自动识别你的系统，点击下载 **Android Studio**（Windows 版 `.exe` 安装包，约 1.2GB）
3. 如果页面没自动识别，手动选 `Download Android Studio` → 选 **Windows (64-bit)**

> 这是 Google 官方站点。Android Studio 内含 JDK、Android SDK、Gradle，**装这一个就够了**，不需要再单独装 Java 或 Gradle。

### 第 2 步：安装 Android Studio

1. 双击下载的 `.exe` 文件启动安装向导
2. 到 **Choose Components** 这一步，保持默认勾选（Android Studio + Android Virtual Device），点 Next
3. 安装路径建议保持默认 `C:\Program Files\Android\Android Studio`，不要放含中文或空格的目录
4. 安装完成后点 **Finish**，勾选 **Start Android Studio** 启动

### 第 3 步：首次启动初始化

首次启动会弹出设置向导（Setup Wizard），按下面选：

1. 弹出 **Import Settings** → 选 **Do not import settings** → OK
2. 弹出数据分享 → 选 **Don't send** 或 **Send** 都行，随意
3. 到 **Install Type** → 选 **Standard** → Next
4. 到 **Select UI Theme** → 随意选 → Next
5. 到 **Verify Settings** → 列出将要下载的 SDK Components → Next
6. 弹出许可协议 → 每一项点 Accept → Finish
7. 等待下载 Android SDK、build-tools、platform-tools 等（约 2GB，5-20 分钟取决于网速）

> 下载完成后，Android Studio 会停在欢迎页（Welcome）。

### 第 4 步：确认 SDK 安装成功

打开 Windows **命令提示符**（按 `Win+R` 输入 `cmd` 回车），执行：

```
dir "%LOCALAPPDATA%\Android\Sdk\platforms"
```

能看到 `android-36` 目录就说明 SDK 装好了。

### 第 5 步：克隆本项目

如果没装 Git，去下载安装：https://git-scm.com/download/win （一路 Next 即可）

然后在命令提示符里执行：

```
git clone <repo-url> AutoClickPhone
cd AutoClickPhone
```

或者直接在 Android Studio 里克隆：
- Welcome 页点 **Get from VCS**
- 填入仓库 URL，选本地目录，点 Clone

### 第 6 步：用 Android Studio 打开工程

1. Welcome 页点 **Open**（或在已打开的 Studio 里 `File` → `Open`）
2. 选择 `AutoClickPhone` 根目录（**注意：是含 `settings.gradle.kts` 的那一层**）
3. 弹出 **Trust Project** → 选 **Trust Project**
4. 右下角会显示 **Gradle Sync** 进度条：
   - 首次会自动下载 Gradle 9.8.2（约 130MB）和全部依赖库（约 500MB）
   - 耐心等 10-20 分钟，期间不要关闭
5. Sync 完成后，底部 **Build** 面板显示 `BUILD SUCCESSFUL`，项目结构树可展开

> 如果网络慢，卡在下载 Gradle：可以在 `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle` 里配置代理，或参考 https://mirrors.cloud.tencent.com/gradle/ 手动下载后放到 `%USERPROFILE%\.gradle\wrapper\dists\gradle-9.8.2-bin\` 对应目录。

### 第 7 步：连接手机或模拟器

#### 方式 A：真机（推荐，无障碍功能必须真机验证）

1. 手机 → **设置** → **关于手机** → 连点 **版本号** 7 次开启开发者模式
2. 返回 → **开发者选项** → 打开 **USB 调试**
3. 用 USB 线连电脑，手机弹"是否允许 USB 调试"→ **允许**
4. Android Studio 顶部工具栏的设备下拉框里能看到你的手机型号

#### 方式 B：模拟器（仅用于 UI 预览，无障碍功能在模拟器上可能受限）

1. Android Studio 顶部点 **Device Manager** → **Create Device**
2. 选一个手机型号（如 Pixel 7）→ Next
3. 选系统镜像 **API 34**（或更高）→ Download → Next → Finish
4. 点 ▶ 启动模拟器

### 第 8 步：运行

1. Android Studio 顶部确认选了 `app` 和你的设备
2. 点 **▶ Run** 按钮（或 `Shift+F10`）
3. 等待编译打包 → 自动安装到设备 → App 打开

---

## 命令行构建（可选）

不打开 Android Studio，纯命令行也能编译。打开 **命令提示符**：

```
cd AutoClickPhone

REM 编译 debug APK
gradlew.bat assembleDebug

REM 运行单元测试
gradlew.bat testDebugUnitTest

REM 安装到已连接设备
gradlew.bat installDebug
```

产物路径：`app\build\outputs\apk\debug\app-debug.apk`

> Windows 下用 `gradlew.bat`，Mac/Linux 下用 `./gradlew`。

---

## 环境要求一览

| 项目 | 版本 | 来源 |
|---|---|---|
| 操作系统 | Windows 10/11 64 位 | — |
| Android Studio | 2026.2 及以上 | 官网下载，含 JDK 25 |
| JDK | 25 | Android Studio 自带，无需单独装 |
| Android SDK | Platform 36 | Studio 首次启动自动下载 |
| Gradle | 9.8.2 | wrapper 自动下载 |
| Kotlin | 2.2.21 | AGP 内置 |
| 最低运行设备 | Android 7.0 (API 24) | `dispatchGesture` 最低要求 |
| 目标设备 | Android 14 (API 34) | — |

---

## 依赖库

以下库在 Gradle Sync 时自动从 Google Maven / Maven Central 下载，无需手动操作。完整版本见 [`gradle/libs.versions.toml`](gradle/libs.versions.toml)。

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

## 首次运行权限

App 安装后需依次开启以下权限（App 内"设置"页有引导按钮）：

1. **无障碍服务** — 核心功能必需，用于读取屏幕节点和模拟点击
2. **悬浮窗权限** — 悬浮球控制
3. **电池优化白名单** — 防止后台被杀

---

## 预置 Demo

App 内点击 **导入 Demo 脚本** 可加载示例（对系统设置 App 执行 `launchApp → waitFor → back` 流程），用于冒烟验证。
