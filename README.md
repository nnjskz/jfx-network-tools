# 🛠️ JFX Network Tools

&#x20;&#x20;

**JFX Network Tools** 是一款macOS端的网络调试工具（基于JavaFX），支持 TCP/UDP 客户端与服务端模式。界面清晰，适用于网络协议调试、IoT 设备测试、教学演示、快速原型开发等场景。

---

## ⭐ 功能亮点

| 功能                      | 说明                          |
|-------------------------|-----------------------------|
| ✅ **TCP/UDP 客户端 & 服务端** | 双模式支持，便捷切换                  |
| 🔄 **自动应答**             | 适合模拟服务器（固定响应，后续考虑开发自定义响应内容） |
| 📜 **连接历史记录**           | 快速复用历史配置                    |
| 🗄 **日志保存**             | 支持将数据保存为日志，方便预览、分析          |
| 🌙 **深色/浅色主题**          | 自动切换，兼容操作系统                 |
| 🗂 **定时发送**             | 支持一键定时发送，模拟心跳数据             |

---

## 📸 软件截图
### ☀️浅色主题
<div>
<img src="docs/img/v1.1/light01.png" width="250" alt="">
<img src="docs/img/v1.1/light02.png" width="250" alt="">
<img src="docs/img/v1.1/light03.png" width="250" alt="">
<img src="docs/img/v1.1/light04.png" width="250" alt="">
<img src="docs/img/v1.1/light05.png" width="250" alt="">
<img src="docs/img/v1.1/light06.png" width="250" alt="">
<img src="docs/img/v1.1/light07.png" width="250" alt="">
</div>


### 🌛深色主题
<div>
<img src="docs/img/v1.1/night01.png" width="250" alt="">
<img src="docs/img/v1.1/night02.png" width="250" alt="">
<img src="docs/img/v1.1/night03.png" width="250" alt="">
<img src="docs/img/v1.1/night04.png" width="250" alt="">
<img src="docs/img/v1.1/night05.png" width="250" alt="">
<img src="docs/img/v1.1/night06.png" width="250" alt="">
<img src="docs/img/v1.1/night07.png" width="250" alt="">
</div>

---

## 🚀 安装与使用

### 1. 开发环境要求

- Java 17+
- JavaFX 17.0.6（已集成）
- 本地根据Maven Profiles启动

### 2.关于jlink模块化打包
本项目使用了 RichTextFX 组件库。由于该库默认未提供模块化（即没有 module-info.java），
为实现基于 jlink 的模块化打包，我们手动为其以及相关依赖（如 Flowless、UndoFX、ReactFX、WellBehavedFX）补充了 module-info 并打包为模块化 JAR。

操作步骤详见 `/build-modular/richtextfx` 目录，具体包括：

1. 解压 JavaFX SDK（根据你的操作系统下载对应的sdk）；
2. 运行 `build.sh` 脚本自动完成依赖模块化并安装至本地 Maven 仓库；
3. 即可使用jlink生成可运行镜像。

该过程仅需本地一次性执行，打包后的模块已支持 jlink 运行时构建。

---

## 📁 数据文件存储

✅ 应用会自动创建数据、日志文件于用户目录下：

- macOS: `~/Library/Application Support/JFXNetworkTools/`

数据目录不会随 `.app` 删除，支持持久配置与历史记录。

---

## 🤝 开源协议

本项目采用 **MIT License**，详见 [LICENSE](./LICENSE)。 欢迎提交 **Issue、建议或 bug 报告**，共建更好网络调试工具！

---
## 🙏 致谢
- [RichTextFX](https://github.com/FXMisc/RichTextFX)（BSD-2-Clause License）
- [AtlantaFX](https://github.com/mkpaz/atlantafx)（MIT License）
---
## 📦 贡献与支持

- 🌱 欢迎提出功能建议
- 🐛 Bug 报告请附完整日志与复现步骤

---