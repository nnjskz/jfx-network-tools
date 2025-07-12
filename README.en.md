# 🛠️ JFX Network Tools

**JFX Network Tools** is a network debugging tool for macOS (based on JavaFX), supporting TCP/UDP client and server modes. It has a clear interface and is suitable for scenarios such as network protocol debugging, IoT device testing, teaching demonstrations, and rapid prototyping.

---

## ⭐ Features

| Feature                        | Description                                                 |
|-------------------------------|-------------------------------------------------------------|
| ✅ **TCP/UDP Client & Server** | Dual-mode support, easy to switch between modes             |
| 🔄 **Auto Response**           | Suitable for simulation servers (fixed response, consider developing custom response content later) |
| 📜 **Connection History**       | Quickly reuse previously connected configurations           |
| 🗄 **Log Saving**              | Support saving data as logs for easy preview and analysis  |
| 🌙 **Dark/Light Theme**        | Auto theme switching with OS compatibility                  |
| 🗂 **Scheduled Send**          | Support one-key scheduled sending, simulate heartbeat data    |

---

## 📸 Screenshots

### ☀️ Light Theme
<img src="docs/img/v1.1/light01.png" width="250" alt="">
<img src="docs/img/v1.1/light02.png" width="250" alt="">
<img src="docs/img/v1.1/light03.png" width="250" alt="">
<img src="docs/img/v1.1/light04.png" width="250" alt="">
<img src="docs/img/v1.1/light05.png" width="250" alt="">
<img src="docs/img/v1.1/light06.png" width="250" alt="">
<img src="docs/img/v1.1/light07.png" width="250" alt="">

### 🌛 Dark Theme
<img src="docs/img/v1.1/night01.png" width="250" alt="">
<img src="docs/img/v1.1/night02.png" width="250" alt="">
<img src="docs/img/v1.1/night03.png" width="250" alt="">
<img src="docs/img/v1.1/night04.png" width="250" alt="">
<img src="docs/img/v1.1/night05.png" width="250" alt="">
<img src="docs/img/v1.1/night06.png" width="250" alt="">
<img src="docs/img/v1.1/night07.png" width="250" alt="">

---

## 🚀 Installation & Usage

### 1. Development Requirements

- Java 17+
- JavaFX 17.0.6 (bundled)
- Use Maven profiles to start the app locally

### 2. About jlink modular packaging
This project uses the RichTextFX component library. Since the library does not provide modularization by default (i.e., there is no module-info.java),
in order to implement modular packaging based on jlink, we manually added module-info to it and related dependencies (such as Flowless, UndoFX, ReactFX, WellBehavedFX) and packaged them into modular JARs.
For detailed operation steps, please refer to the `/build-modular/richtextfx` directory, including:
1. Unzip the JavaFX SDK (download the corresponding SDK according to your operating system);
2. Run the `build.sh` script to automatically complete the dependency modularization and install it to the local Maven repository;
3. You can use jlink to generate a runnable image.

This process only needs to be executed locally once, and the packaged module already supports jlink runtime build.

---

## 📁 Data Storage

✅ The application automatically creates a data folder in the user directory:

- macOS: `~/Library/Application Support/JFXNetworkTools/`

This folder is persistent and won't be deleted when uninstalling the app.

---

## 🤝 License

This project is licensed under the **MIT License**. See [LICENSE](./LICENSE) for details. Feel free to submit **issues, suggestions, or bug reports** to help us improve!

---
## 🙏 Acknowledgements
- [RichTextFX](https://github.com/FXMisc/RichTextFX)（BSD-2-Clause License）
- [AtlantaFX](https://github.com/mkpaz/atlantafx)（MIT License）
---
## 📦 Contributions

- 🌱 Feature requests are welcome
- 🐛 Please include logs and steps when reporting bugs

---

