# AutoClickPhone 设计文档

纯手机端 Android 自动点击工具。面向非技术人员，通过无障碍服务（AccessibilityService）实现对原生 UI App 的自动点击、滑动等操作，无需 root。

## 1. 项目概述

### 1.1 目标

- 在手机上独立运行（不依赖电脑），自动化原生 UI App 中的重复操作（如签到、领奖励、每日任务）。
- 非技术人员可以通过**录制**或**可视化编辑**创建自动化脚本，无需写代码。
- 脚本可导入导出，方便分享。

### 1.2 非目标（当前版本）

- 不做游戏/画布类应用的图像识别自动化（列为后续扩展）。
- 不做 iOS。
- 不做云控/多设备管理。

### 1.3 约束与前提

- 目标设备：Android 7.0（API 24）及以上（`dispatchGesture` 的最低要求）。
- 无需 root。
- 目标 App 为原生 UI（有完整无障碍节点树）。

## 2. 技术选型

| 项目 | 选择 | 理由 |
|---|---|---|
| 语言 | Kotlin | Android 官方首选，协程适合脚本引擎的异步执行 |
| 最低 SDK | API 24（Android 7.0） | `AccessibilityService.dispatchGesture()` 最低版本 |
| 目标 SDK | API 34 | 满足 Google Play / 主流 ROM 要求 |
| UI 框架 | Jetpack Compose + Material 3 | 声明式 UI，代码量少 |
| 手势模拟 | AccessibilityService + `dispatchGesture()` | 无 root 模拟点击/滑动；可点击节点也可 `performAction(ACTION_CLICK)` |
| 元素定位 | 无障碍节点树（viewId / text / contentDescription / bounds） | 原生 UI App 天然支持，稳定且快 |
| 脚本格式 | JSON 步骤流 | 可视化编辑器的数据模型，方便导入导出 |
| 异步 | Kotlin Coroutines + Flow | 引擎逐步执行、超时控制、日志流 |
| 存储 | Room | 脚本、运行日志的持久化 |
| 依赖注入 | 手动注入（Hilt 暂不引入） | 项目初期保持简单，模块少 |
| 构建 | Gradle (Kotlin DSL) | 标准配置 |

### 2.1 为什么不用其他方案

| 方案 | 不选原因 |
|---|---|
| ADB + PC 控制 | 用户要求纯手机端独立运行 |
| root + sendevent 记录回放 | 需要 root（清数据、失保修），门槛高 |
| UiAutomator2 / Appium | 依赖 PC 端或 atx-agent，且主要面向测试场景 |
| 截图 + OpenCV 图像识别 | 只适用于游戏/画布；原生 UI 用节点树定位更快更稳，图像识别作为兜底扩展 |

## 3. 系统架构

```
┌─────────────────────────────────────────────┐
│  UI 层                                       │
│  脚本列表 / 步骤编辑器 / 悬浮球(开始·停止·准星)  │
├─────────────────────────────────────────────┤
│  引擎层  ScriptEngine                        │
│  解析 JSON → 逐步执行 → 循环/条件/重试/失败截图  │
├──────────────┬──────────────────────────────┤
│  定位层       │  动作层                       │
│  ElementLocator │ GestureDispatcher          │
│  节点树查找、   │  tap/longPress/swipe/        │
│  选择器匹配    │  back/home/launchApp/输入文本  │
├──────────────┴──────────────────────────────┤
│  服务层                                       │
│  AutoClickAccessibilityService(核心服务)      │
│  ForegroundService(保活)  OverlayManager     │
├─────────────────────────────────────────────┤
│  数据层  Room: 脚本 / 步骤 / 运行日志          │
└─────────────────────────────────────────────┘
```

### 3.1 模块职责

| 模块 | 职责 |
|---|---|
| `service` | 无障碍服务：节点树获取、手势派发、窗口事件监听、文本输入 |
| `engine` | 脚本引擎：解析步骤、执行流程控制（循环/条件/重试/超时）、产生运行事件 |
| `locator` | 元素定位：按 viewId / text / desc / bounds 查找节点，选择器匹配策略 |
| `action` | 动作封装：tap、longPress、swipe、back、home、launchApp、inputText、delay、waitFor |
| `overlay` | 悬浮窗管理：控制球（开始/停止）、录制准星 |
| `data` | Room 数据库：脚本 CRUD、运行日志写入与查询 |
| `ui` | Compose 界面：脚本列表、步骤编辑器、设置、权限引导 |
| `recorder` | 录制器：准星模式下解析用户选中的节点，生成步骤 |

### 3.2 执行时序（核心链路）

```
用户点悬浮球"开始"
  → OverlayManager 通知 EngineController
  → ScriptEngine 启动协程，逐条执行步骤:
      1. ElementLocator 在 rootInActiveWindow 中按选择器查找节点
      2. 找到 → GestureDispatcher.dispatchGesture(tap, 节点中心坐标)
         找不到 → 等待轮询直到超时 → 步骤失败(可重试)
      3. 每步结果写入运行日志(Logcat + Room)
  → 全部完成 / 用户点"停止" / 致命错误 → 引擎收尾，悬浮球复位
```

## 4. 脚本格式设计

### 4.1 顶层结构

```json
{
  "version": 1,
  "name": "每日签到",
  "targetPackage": "com.example.app",
  "repeat": 1,
  "steps": [ ... ]
}
```

### 4.2 步骤类型（v1 全集）

| action | 说明 | 关键字段 |
|---|---|---|
| `click` | 点击元素或坐标 | `target`（元素选择器）或 `point`（坐标） |
| `longPress` | 长按 | 同上，`duration` 可选 |
| `swipe` | 滑动 | `from`/`to` 或 `direction` + `distance`，`duration` |
| `back` | 返回键 | — |
| `home` | 回桌面 | — |
| `launchApp` | 启动 App | `packageName` |
| `inputText` | 向焦点输入框输入文本 | `text`（使用无障碍 `ACTION_SET_TEXT`） |
| `waitFor` | 等待元素出现 | `target`，`timeout`（秒，默认 10） |
| `delay` | 固定等待 | `seconds` |
| `scrollTo` | 滑动直到找到元素 | `target`，`maxSwipes`（默认 10） |

通用可选字段：`retry`（失败重试次数，默认 0）、`timeout`（秒）。

### 4.3 元素选择器（target）

定位优先级：`id` > `text` > `textContains` > `desc` > `point`（坐标兜底）。

```json
{ "id": "com.example.app:id/btn_sign" }
{ "text": "立即签到" }
{ "textContains": "签到", "className": "android.widget.Button" }
{ "point": { "x": 540, "y": 1800 }, "anchor": "top-left" }
```

- `text` 精确匹配，`textContains` 包含匹配，`desc` 匹配 contentDescription。
- 录制时优先记录 `id`，无 id 则记录 `text`/`desc`，都无则回退坐标。
- `anchor` 用于坐标在不同分辨率下的换算（可选 `center`，默认按比例存储）。

### 4.4 示例

```json
{
  "version": 1,
  "name": "每日签到",
  "targetPackage": "com.example.app",
  "repeat": 1,
  "steps": [
    { "action": "launchApp", "packageName": "com.example.app" },
    { "action": "waitFor", "target": { "text": "我的" }, "timeout": 10 },
    { "action": "click", "target": { "text": "我的" } },
    { "action": "scrollTo", "target": { "id": "com.example.app:id/btn_sign" }, "maxSwipes": 5 },
    { "action": "click", "target": { "id": "com.example.app:id/btn_sign" }, "retry": 3 },
    { "action": "delay", "seconds": 1 },
    { "action": "back" }
  ]
}
```

### 4.5 扩展预留（v2+）

- `ifVisible` / `loop` 控制流步骤。
- 图像匹配步骤：`{ "action": "click", "target": { "image": "sign_btn.png" } }`（MediaProjection 截图 + 模板匹配）。
- 步骤分组、脚本间调用。

## 5. 关键设计细节

### 5.1 无障碍服务配置

`accessibility_service_config.xml`：

- `canRetrieveWindowContent="true"` — 读取节点树。
- `canPerformGestures="true"` — 派发手势（点击/滑动必需）。
- 监听事件：`typeWindowStateChanged`、`typeWindowContentChanged`（用于 waitFor 的被动通知优化，v1 可先用轮询）。
- `packageNames` 不设过滤（需要跨 App 操作，如回到桌面、多 App 流程）。

### 5.2 引擎执行模型

- 每次运行一个 `ScriptEngine` 实例，绑定一个协程（`SupervisorJob`），步骤串行执行。
- 支持取消：停止按钮 → `job.cancel()`，手势回调取消。
- 步骤超时用 `withTimeout` 包裹；`waitFor` 用 500ms 间隔轮询节点树（v1），v2 改事件驱动。
- 单步失败策略：`retry` 次数内重试；重试耗尽 → 默认终止脚本并记录日志（v2 增加"继续下一步"选项）。

### 5.3 悬浮球与录制

- 悬浮球常驻（可拖动、贴边隐藏），点开展开：开始/停止、录制、设置。
- 权限：`SYSTEM_ALERT_WINDOW`（Android 11+ 用 `Settings.canDrawOverlays` 引导）。
- **录制流程（准星模式）**：
  1. 用户点"录制"，启动目标 App。
  2. 屏幕出现半透明准星（一个可拖动的小圆点）+ 底部"添加步骤"按钮。
  3. 用户把准星拖到目标元素上 → 点击"添加步骤" → Recorder 取准星坐标，在节点树中找包含/最近该坐标的**可点击祖先节点**，生成一条 `click` 步骤（自动选择最优选择器）。
  4. 重复 3，录制滑动（在准星模式下按住拖动即记录 swipe 轨迹）。
  5. 点"完成" → 生成 JSON 存入 Room，进入编辑器可微调（改 timeout、加 delay 等）。
- 录制生成的脚本默认带 0.5~1s 的步骤间隔（可通过"执行速度"设置调节）。

### 5.4 保活策略

- `ForegroundService` + 常驻通知（运行中显示当前步骤），满足 Android 8+ 后台限制。
- `WakeLock`（`PARTIAL_WAKE_LOCK`）保持运行期间不锁屏中断。
- 首次运行引导页检测并引导：无障碍权限、悬浮窗权限、电池优化白名单（`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`）。
- 国产 ROM 额外引导文案：自启动、锁后台（通用文案提示，不做机型判断）。

### 5.5 服务与引擎的连接

- `AutoClickAccessibilityService` 是系统回调入口，不持有业务状态；引擎通过它访问节点树和派发手势。
- 连接方式：静态持有服务引用（`WeakReference`）+ `ServiceBinder` 接口，UI/引擎只依赖接口，便于测试时替换 Fake。

```kotlin
interface AccessibilityGateway {
    fun findNodes(selector: Selector): List<NodeInfo>
    fun rootNodes(): List<NodeInfo>
    fun dispatchTap(x: Float, y: Float, duration: Long): Boolean
    fun dispatchSwipe(from: Point, to: Point, duration: Long): Boolean
    fun pressBack(): Boolean
    fun inputText(text: String): Boolean
    fun launchApp(packageName: String): Boolean
}
```

引擎、定位器、录制器全部只依赖 `AccessibilityGateway`，无障碍服务是实现类。

## 6. 数据模型（Room）

```
ScriptEntity      id, name, targetPackage, repeat, json(steps), createdAt, updatedAt, sortOrder
RunLogEntity      id, scriptId, startedAt, finishedAt, status(RUNNING/SUCCESS/FAILED/CANCELED),
                  currentStepIndex, errorMessage
StepLogEntity     id, runId, stepIndex, action, status, detail(选择器/坐标/耗时), timestamp
```

- 脚本正文以 JSON 字符串存储（非技术人员不需要按步骤查询数据库），列表页只读元信息。
- 运行日志保留最近 N 条（默认 50 次运行，可在设置里清空）。

## 7. 项目结构（单模块，按包分层）

```
app/src/main/java/com/autoclick/app/
├── AutoClickApp.kt                     # Application
├── MainActivity.kt                     # 单 Activity 宿主
├── service/
│   ├── AutoClickAccessibilityService.kt
│   └── AccessibilityGatewayImpl.kt     # Gateway 实现
├── engine/
│   ├── ScriptEngine.kt                 # 执行器
│   ├── ScriptParser.kt                 # JSON ↔ 领域模型
│   ├── StepExecutor.kt                 # 各 action 的执行
│   └── RunEvent.kt                     # 运行事件（Flow 发射）
├── locator/
│   ├── Selector.kt                     # 选择器模型
│   ├── ElementLocator.kt               # 节点查找与匹配
│   └── NodeInfo.kt                     # 节点快照（脱离 Parcelable）
├── recorder/
│   └── StepRecorder.kt                 # 准星 → 步骤生成
├── overlay/
│   ├── OverlayManager.kt
│   ├── ControlBubbleView.kt            # 悬浮球
│   └── CrosshairView.kt                # 录制准星
├── data/
│   ├── AppDatabase.kt
│   ├── ScriptRepository.kt
│   ├── RunLogRepository.kt
│   └── entity/…
├── keepalive/
│   └── EngineForegroundService.kt      # 前台服务（保活）
└── ui/
    ├── nav/                            # Compose Navigation
    ├── home/                           # 脚本列表
    ├── editor/                         # 步骤编辑器
    ├── runlog/                         # 运行日志
    └── setup/                          # 权限引导
```

## 8. 风险与限制

| 风险 | 影响 | 对策 |
|---|---|---|
| 无障碍权限被系统回收（部分 ROM 杀后台后自启失败） | 脚本无法运行 | 引导用户锁定后台/电池白名单；运行前检测服务连接状态并提示 |
| 微信、银行类 App 反无障碍检测 | 目标 App 拒绝运行或弹风控 | 文档中明确告知；不做绕过（合规风险） |
| `dispatchGesture` 事件带系统标记，与真实触摸有差异 | 极少数 App 识别为机器操作 | 无低成本对策，接受该限制并写入文档 |
| 国产 ROM 电池策略激进 | 长脚本中途被杀 | 前台服务 + WakeLock + 引导；关键脚本提示分拆 |
| 节点树拿不到某些自绘控件 | 定位失败 | 坐标兜底（录制时记录 bounds 比例）；v2 图像识别 |
| 无障碍服务被系统更新重置 | 需要重新开启 | 启动时检测权限状态，红点提醒 |

## 9. 实施路线

| 阶段 | 内容 | 验收标准 |
|---|---|---|
| M1 骨架 | 工程搭建、无障碍服务、Gateway、Room、手写 JSON 脚本可跑通 click/waitFor/back/launchApp | 预置 JSON 能在真机完成一次签到式流程 |
| M2 动作全集 | swipe/longPress/scrollTo/inputText/delay、重试与超时、运行日志页 | 10 类步骤全部可用，失败可追溯 |
| M3 悬浮球与录制 | 悬浮球控制、准星录制、步骤自动生成 | 非技术人员 5 分钟内录出一个可回放的脚本 |
| M4 编辑器 | 步骤可视化编辑（增删改排序）、导入导出分享 | 不接触 JSON 也能完整创建/修改脚本 |
| M5 打磨 | 权限引导页、设置（执行速度/日志上限）、崩溃上报 | 新用户无引导文档可独立上手 |

## 10. 测试策略

- 单元测试：ScriptParser（JSON 解析/容错）、Selector 匹配逻辑、ElementLocator（用 Fake Gateway）。
- 手工真机测试矩阵：至少 1 台原生系（Pixel/模拟器 API 34）+ 1 台国产 ROM（小米/华为）。
- 集成冒烟：预置 demo 脚本对系统设置 App 做点击（不依赖第三方 App 的不确定性）。
