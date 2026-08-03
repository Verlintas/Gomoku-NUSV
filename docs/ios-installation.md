# iOS 安装指南（无需开发者账号）

iPhone 默认只能从 App Store 安装应用。本项目的 iOS 版尚未上架 App Store，
但你可以通过以下任一种方式把它装到自己的 iPhone 上，**都不需要付费的
Apple Developer 账号（$99/年）**。

> 适用版本：`Gomoku-NUSV-1.4.6-compatibility.ipa`（未签名，见下方获取方式）
> 或直接从源码用 Xcode 构建。

---

## 方案 A：Xcode 免费个人签名（官方途径，推荐）

用免费 Apple ID 在 Xcode 中签名并安装，安全性最高、过程最可控。

### 准备

- macOS 电脑，已安装 Xcode（本教程基于 Xcode 26）
- 数据线（可信任的 USB 线）
- iPhone（iOS 14+），以及一个 Apple ID（免费即可）

### 步骤

1. **打开工程**
   ```bash
   open iosApp/iosApp.xcodeproj
   ```

2. **登录 Apple ID**
   Xcode 菜单 → Settings → Accounts → 左下角 `+` → Apple ID → 登录你的账号。
   若提示选择团队，选 **Personal Team**（免费）。

3. **配置签名**
   - 左侧选中 `iosApp` 项目 → `iosApp` target
   - **Signing & Capabilities** 标签页
   - 勾选 **Automatically manage signing**
   - **Team** 选择刚添加的 Personal Team

4. **连接 iPhone**
   - 数据线连接电脑与 iPhone
   - iPhone 上点击“信任此电脑”
   - Xcode 顶部运行目的地选择你的 iPhone（若未出现，Window → Devices
     and Simulators 确认设备被识别）

5. **运行**
   按 `Cmd + R`。首次构建需几分钟（编译 Kotlin framework）。

6. **信任开发者证书**
   首次运行时 iPhone 会提示“未受信任的开发者”，前往
   **设置 → 通用 → VPN 与设备管理** → 找到你的 Apple ID → 点击“信任”。

### 限制

- 免费签名安装的应用 **7 天后过期**（打开会闪退）。
- 续期方法：重新连接电脑，在 Xcode 里再次 `Cmd + R` 即可（进度条走完即续期）。
- 同一时间本机最多保留 3 个免费签名应用（不影响正常使用）。

---

## 方案 B：AltStore 侧载（无线续签）

AltStore 是开源的侧载工具，最大优点是**支持 7 天自动续签**（电脑开着
AltServer 即可），无需每次重连数据线。

### 准备

- 电脑（Windows 或 macOS）安装 AltServer：
  - Windows：<https://altstore.io> 下载 AltServer for Windows
  - macOS：下载 AltServer for macOS（需 macOS 10.15+）
- iPhone 安装 AltStore 客户端（由 AltServer 注入）
- 一个 Apple ID

### 步骤

1. **电脑安装 AltServer 并运行**（macOS 首次运行需在
   “系统设置 → 隐私与安全性”允许；Windows 需安装 iCloud/Apple Devices）。
2. **手机连数据线**，在 AltServer 菜单里选择你的设备 →
   **Install AltStore**，输入 Apple ID 和密码（App 专用密码更安全）。
3. iPhone 桌面出现 **AltStore** 图标；首次打开按提示信任开发者证书
   （设置 → 通用 → VPN 与设备管理）。
4. **安装游戏**：
   - 电脑浏览器下载 `Gomoku-NUSV-1.4.6-compatibility.ipa`
   - iPhone 的 **文件 App**（iCloud 或“我的 iPhone”）导入该 .ipa
   - 打开 AltStore → **我的应用** 标签 → 左上角 `+` → 选择该 .ipa → 安装
   - AltStore 会自动用你的 Apple ID 重签名安装
5. **续签**：AltStore 会在 7 天到期前尝试自动刷新（需电脑 AltServer
   与手机在同一网络且 AltServer 在线）。也可手动在 AltStore 里点刷新。

### 说明

- AltStore 免费版限 3 个应用，够用。
- 使用 AltStore 需要保持 AltServer 偶尔在线，否则到期需重连电脑刷新。
- 侧载属于 Apple 允许的个人用途（本机测试），不会影响保修或系统安全；
  但请只安装信任来源的 ipa。

---

## 方案 C：爱思助手（中国大陆常用）

爱思助手（<https://www.i4.cn>）支持“未签名安装”，自动用你的 Apple ID
完成签名，操作最简单。

### 步骤

1. 电脑安装并打开 **爱思助手**，iPhone 数据线连接（首次需在手机上信任电脑）。
2. 点击顶部“应用游戏” → 右侧“导入安装” → 选择
   `Gomoku-NUSV-1.4.6-compatibility.ipa`。
3. 按提示输入 Apple ID（爱思会引导生成“专用密码”），等待自动签名安装。
4. 手机出现应用图标；首次打开到
   **设置 → 通用 → VPN 与设备管理** 信任开发者证书。
5. 7 天到期后，重新用爱思助手“修复/重装”即可续期。

### 注意

- 爱思助手属于第三方工具，建议从官网下载，安装时留意捆绑软件勾选项。
- 如果不想用第三方工具，推荐方案 A 或 B。

---

## 常见问题

**Q：装好后打不开，提示“未受信任的开发者”？**
去 设置 → 通用 → VPN 与设备管理 → 找到开发者证书 → 点“信任”。

**Q：7 天后应用闪退？**
免费签名的正常现象。按对应方案重新签名/续期即可，存档数据不会丢
（应用内数据在沙盒中，重装后仍在——除非删除应用）。

**Q：需要越狱吗？**
不需要。以上三种方案都无需越狱。

**Q：能上架 App Store 吗？**
需要付费的 Apple Developer Program（$99/年）并通过审核，当前版本尚未上架。

---

## 获取 .ipa

- 从 GitHub Release 下载：`Gomoku-NUSV-1.4.6-compatibility.ipa`
  （https://github.com/Verlintas/Gomoku-NUSV/releases）
- 或自行构建（macOS + Xcode）：

  ```bash
  # 1. 构建未签名真机版本
  xcodebuild -project iosApp/iosApp.xcodeproj -target iosApp \
    -sdk iphoneos -configuration Debug ARCHS=arm64 ONLY_ACTIVE_ARCH=YES \
    CODE_SIGNING_ALLOWED=NO build

  # 2. 打包成 ipa（Payload 结构）
  cd iosApp/build/Debug-iphoneos
  mkdir -p /tmp/ipa/Payload
  cp -r Gomoku-NUSV.app /tmp/ipa/Payload/
  cd /tmp/ipa && zip -r Gomoku-NUSV.ipa Payload
  ```

> 免责声明：侧载仅用于在自有设备上安装测试。请从本仓库官方 Release
> 获取 ipa，避免使用来路不明的修改版本。
