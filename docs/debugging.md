# 调试指南

本文档介绍如何将 AutoClickPhone 安装到手机、运行脚本、查看日志和断点调试。

> 无障碍服务功能必须在**真机**上验证，模拟器上可能受限。以下以 Windows + 真机为例。

---

## 1. 准备真机

### 1.1 开启开发者模式

1. 手机 → **设置** → **关于手机**
2. 找到 **版本号**（部分机型在"软件信息"里）
3. 连续点击 **版本号** 7 次
4. 出现提示"您现在处于开发者模式"

### 1.2 开启 USB 调试

1. 手机 → **设置** → **系统** → **开发者选项**（部分机型在"设置"最底部）
2. 打开 **开发者选项** 总开关
3. 打开 **USB 调试**
4. （可选）打开 **USB 安装**，允许通过 USB 安装应用

### 1.3 连接电脑

1. 用 **数据线**（不是纯充电线）连接手机和电脑
2. 手机下拉通知栏 → USB 连接方式选 **文件传输 (MTP)**
3. 手机弹出"是否允许 USB 调试？"→ 勾选"始终允许"→ **确定**

### 1.4 验证连接

打开命令提示符，执行：

```
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" devices
```

输出类似下面这样，说明连接成功：

```
List of devices attached
XXXXXXXX    device
```

> 如果显示 `unauthorized`，检查手机上是否有"允许调试"弹窗未点。
> 如果什么都没有，换一根数据线或换一个 USB 口。

---

## 2. 安装到手机

### 方式一：Android Studio（推荐）

1. Android Studio 顶部工具栏，确认：
   - 左侧下拉框选 **app**
   - 右侧下拉框选 **你的手机型号**
2. 点 **▶ Run** 按钮（或按 `Shift + F10`）
3. 等待编译 → 打包 → 安装 → 自动启动 App

### 方式二：命令行

```
cd AutoClickPhone
gradlew.bat installDebug
```

安装成功后手机上会出现 AutoClickPhone 图标，手动点开即可。

> 重新安装：`gradlew.bat uninstallDebug && gradlew.bat installDebug`

---

## 3. 授予权限

App 首次打开后，进入底部导航栏的 **设置** 页，依次开启三项权限：

### 3.1 无障碍服务（必需）

1. 点"无障碍服务"右侧的 **去设置**
2. 系统跳转到无障碍设置页
3. 找到 **AutoClickPhone**（或"已下载的服务"里）
4. 点开 → 打开开关 → 确认
5. 返回 App，设置页该权限显示"已开启"

### 3.2 悬浮窗权限

1. 点"悬浮窗权限"右侧的 **去设置**
2. 系统跳转到"显示在其他应用上层"页
3. 找到 **AutoClickPhone** → 打开开关
4. 返回 App

### 3.3 电池优化白名单

1. 点"电池优化白名单"右侧的 **去设置**
2. 系统弹窗 → 选 **不优化**（或"允许"）
3. 返回 App

> 三项都显示"已开启"后，才能正常运行脚本。

---

## 4. 运行脚本

### 4.1 导入 Demo 脚本

1. 进入底部导航栏的 **脚本** 页
2. 点右上角 📂 图标 → 弹窗选 **导入 Demo**
3. 脚本列表出现"设置App冒烟测试"

### 4.2 运行

1. 点脚本卡片右侧的 **▶ 运行** 按钮
2. 顶部通知栏出现"运行中"通知
3. 手机会自动执行：打开系统设置 → 等待 → 返回

### 4.3 停止

运行过程中无法在 App 内停止（M1 骨架暂未做停止按钮，M3 悬浮球阶段实现）。
当前可通过以下方式中断：

- 下拉通知栏 → 点 AutoClickPhone 通知 → 会被清除
- 或者直接杀掉 App

---

## 5. 查看运行日志

### 5.1 App 内日志

1. 进入底部导航栏的 **日志** 页
2. 每次运行后会出现一条记录，显示：
   - 脚本名称
   - 状态（成功 / 失败 / 已取消）
   - 开始时间、耗时
   - 执行到第几步
   - 错误信息（如失败）
3. 点右上角 🗑 可清空所有日志

### 5.2 Logcat（详细日志）

**在 Android Studio 里：**

1. 底部点 **Logcat** 面板标签
2. 左侧确认选中你的设备
3. 上方过滤栏输入 `package:com.autoclick.app` 回车
4. 能看到 App 运行时输出的所有日志

**在命令行：**

```
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" logcat -s AutoClickPhone
```

只看错误日志：

```
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" logcat *:E
```

---

## 6. 断点调试

### 6.1 打断点

1. 在 Android Studio 打开任意 Kotlin 文件
2. 找到想暂停的代码行，比如 `ScriptEngine.kt` 里 `executeWithRetry` 方法
3. 在行号左侧的灰色区域点一下 → 出现 **红点** = 断点

### 6.2 启动调试

1. 确认顶部选了 **app** 和你的手机
2. 点工具栏 **🐛 Debug** 按钮（Run 按钮旁边的虫子图标）
   - 或按 `Shift + F9`
   - （不是 ▶ Run，Run 不会在断点停）
3. App 安装启动后，正常操作触发断点

### 6.3 调试操作

程序停在断点时，底部出现 **Debug** 面板：

| 按钮 | 快捷键 | 作用 |
|---|---|---|
| 步过 (Step Over) | `F8` | 执行当前行，不进入函数内部 |
| 步入 (Step Into) | `F7` | 进入函数内部 |
| 步出 (Step Out) | `Shift + F8` | 跳出当前函数 |
| 继续 (Resume) | `F9` | 继续运行到下一个断点 |
| 停止 | `Ctrl + F2` | 停止调试 |

在 Debug 面板左侧的 **Variables** 区域，能实时看到当前所有变量的值。
也可以在代码上悬停变量名，弹出气泡显示当前值。

### 6.4 条件断点（进阶）

右键断点红点 → 在 **Condition** 框里输入条件，比如：

```
step.action == "click"
```

只有满足条件时才会暂停，适合在循环里只看特定步骤。

---

## 7. 手动测试自定义脚本

不想写代码，可以直接写 JSON 脚本导入测试。

### 7.1 写脚本

新建一个 `test.json` 文件，内容示例：

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

### 7.2 导入

1. 把 `test.json` 传到手机（微信发自己 / USB 拷贝）
2. App → 脚本页 → 右上角 📂 → **从文件选择**
3. 选到该 JSON 文件
4. 脚本出现在列表中 → 点 ▶ 运行

### 7.3 步骤类型速查

| action | 说明 | 必填字段 |
|---|---|---|
| `click` | 点击元素或坐标 | `target`（选择器）或 `point`（坐标） |
| `longPress` | 长按 | 同上，`duration` 可选 |
| `swipe` | 滑动 | `from`+`to` 或 `direction`+`distance` |
| `back` | 返回键 | — |
| `home` | 回桌面 | — |
| `launchApp` | 启动 App | `packageName` |
| `inputText` | 输入文本 | `text` |
| `waitFor` | 等待元素出现 | `target`，`timeout`（秒，默认 10） |
| `delay` | 固定等待 | `seconds` |
| `scrollTo` | 滑动直到找到元素 | `target`，`maxSwipes`（默认 10） |

### 7.4 选择器写法

| 类型 | 示例 | 说明 |
|---|---|---|
| id | `{ "id": "com.android.settings:id/btn" }` | 精确匹配 viewId |
| text | `{ "text": "蓝牙" }` | 精确匹配文本 |
| textContains | `{ "textContains": "蓝" }` | 包含匹配文本 |
| desc | `{ "desc": "按钮" }` | 匹配 contentDescription |
| point | `{ "point": { "x": 540, "y": 1800 } }` | 坐标兜底 |

---

## 8. 常见问题

| 现象 | 原因 | 解决 |
|---|---|---|
| 运行报"无障碍服务未连接" | 没开无障碍权限 | 设置页 → 无障碍 → 开启 |
| 脚本运行后无反应 | 无障碍服务被系统杀 | 重新开启；锁后台；加电池白名单 |
| `adb devices` 无设备 | USB 调试没开 / 线不对 | 检查 USB 调试；换数据线 |
| 显示 `unauthorized` | 手机弹窗没点 | 拔插 USB，点"允许调试" |
| 编译报 SDK 缺失 | SDK 没装全 | Android Studio → SDK Manager 装 API 36 |
| 点运行后 App 闪退 | 看 Logcat 错误 | Logcat 过滤 `*:E` 查堆栈 |
| 运行卡在 waitFor 超时 | 目标 App 界面没出现该元素 | 检查选择器是否匹配；调大 timeout |
| 通知栏没有运行通知 | 悬浮窗/通知权限没开 | 设置页开启悬浮窗；系统通知权限 |

---

## 9. 卸载

```
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" uninstall com.autoclick.app
```

或在手机上长按 App 图标 → 卸载。
