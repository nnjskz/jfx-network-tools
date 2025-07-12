#!/bin/bash
set -e

# === 配置路径 ===
JAVAFX_LIB="javafx-sdk-17.0.15/lib"  # 请根据你本地路径修改
REACTFX_JAR="reactfx-2.0-M5.jar"
FLOWLESS_JAR="flowless-0.7.4.jar"
RICHTEXTFX_JAR="richtextfx-0.11.5.jar"
WELLBEHAVEDFX_JAR="wellbehavedfx-0.3.3.jar"
UNDOFX_JAR="undofx-2.1.1.jar"

# === 创建输出目录 ===
mkdir -p reactfx-out flowless-out richtextfx-out wellbehavedfx-out undo-out

# ================ReactFX Start=====================
echo "为 ReactFX 添加 module-info.java"
mkdir -p org.reactfx
cat > org.reactfx/module-info.java <<EOF
module reactfx {
    requires javafx.base;
	  requires javafx.graphics;
	  requires javafx.controls;

    exports org.reactfx;
    exports org.reactfx.collection;
    exports org.reactfx.inhibeans;
    exports org.reactfx.inhibeans.binding;
    exports org.reactfx.inhibeans.collection;
    exports org.reactfx.inhibeans.property;
    exports org.reactfx.inhibeans.value;
    exports org.reactfx.value;
	  exports org.reactfx.util;
}
EOF

echo "[2/13] 编译 ReactFX module-info"
javac \
  --module-path "$JAVAFX_LIB" \
  --patch-module reactfx="$REACTFX_JAR" \
  -d reactfx-out \
  org.reactfx/module-info.java

echo "[3/13] 更新 ReactFX JAR"
jar --update --file="$REACTFX_JAR" -C reactfx-out module-info.class

echo "[4/13] 安装 ReactFX 到本地 Maven 仓库"
mvn install:install-file \
  -Dfile="$REACTFX_JAR" \
  -DgroupId=org.reactfx \
  -DartifactId=reactfx \
  -Dversion=2.0-M5-modular \
  -Dpackaging=jar
# ================ReactFX End=====================

# ================Flowless Start=====================
echo "[5/13] 为 Flowless 添加 module-info.java"
mkdir -p org.fxmisc.flowless
cat > org.fxmisc.flowless/module-info.java <<EOF
module org.fxmisc.flowless {
	  requires javafx.controls;
    requires reactfx;

    exports org.fxmisc.flowless;
}

EOF

echo "✅ [6/13] 编译 Flowless module-info"
javac \
  --module-path "$JAVAFX_LIB:$REACTFX_JAR" \
  --patch-module org.fxmisc.flowless="$FLOWLESS_JAR" \
  -d flowless-out \
  org.fxmisc.flowless/module-info.java

echo "[7/13] 更新 Flowless JAR"
jar --update --file="$FLOWLESS_JAR" -C flowless-out module-info.class

echo "[8/13] 安装 Flowless 到本地 Maven 仓库"
mvn install:install-file \
  -Dfile="$FLOWLESS_JAR" \
  -DgroupId=org.fxmisc.flowless \
  -DartifactId=flowless \
  -Dversion=0.7.4-modular \
  -Dpackaging=jar
# ================Flowless End=====================

# ================UndoFX Start=====================
echo "为 UndoFX 添加 module-info.java"
mkdir -p org.fxmisc.undo
cat > org.fxmisc.undo/module-info.java <<EOF
module org.fxmisc.undo {
    requires javafx.base;
	  requires reactfx;

    exports org.fxmisc.undo;
}
EOF

echo "编译 UndoFX module-info"
javac \
  --module-path "$JAVAFX_LIB:$REACTFX_JAR" \
  --patch-module org.fxmisc.undo="$UNDOFX_JAR" \
  -d undo-out \
  org.fxmisc.undo/module-info.java

echo "更新 UndoFX JAR"
jar --update --file="$UNDOFX_JAR" -C undo-out module-info.class

echo "安装 UndoFX 到本地 Maven 仓库"
mvn install:install-file \
  -Dfile="$UNDOFX_JAR" \
  -DgroupId=org.fxmisc.undo \
  -DartifactId=undofx \
  -Dversion=2.1.1-modular \
  -Dpackaging=jar
# ================UndoFX End=====================

# ================WellBehavedFX Start=====================
echo "为 WellBehavedFX 添加 module-info.java"
mkdir -p org.fxmisc.wellbehavedfx
cat > org.fxmisc.wellbehavedfx/module-info.java <<EOF
module wellbehavedfx {
	  requires javafx.controls;

    exports org.fxmisc.wellbehaved.event;
    exports org.fxmisc.wellbehaved.event.internal;
    exports org.fxmisc.wellbehaved.event.template;
}
EOF

echo "编译 WellBehavedFX module-info"
javac \
  --module-path "$JAVAFX_LIB" \
  --patch-module wellbehavedfx="$WELLBEHAVEDFX_JAR" \
  -d wellbehavedfx-out \
  org.fxmisc.wellbehavedfx/module-info.java

echo "更新 WellBehavedFX JAR"
jar --update --file="$WELLBEHAVEDFX_JAR" -C wellbehavedfx-out module-info.class

echo "安装 WellBehavedFX 到本地 Maven 仓库"
mvn install:install-file \
  -Dfile="$WELLBEHAVEDFX_JAR" \
  -DgroupId=org.fxmisc.wellbehavedfx \
  -DartifactId=wellbehavedfx \
  -Dversion=0.3.3-modular \
  -Dpackaging=jar
# ================WellBehavedFX End=====================

# ================RichTextFX Start=====================
echo "为 RichTextFX 添加 module-info.java"
mkdir -p org.fxmisc.richtext
cat > org.fxmisc.richtext/module-info.java <<EOF
module org.fxmisc.richtext {
	  requires javafx.controls;
    requires javafx.fxml;
    requires reactfx;
	  requires org.fxmisc.flowless;
	  requires org.fxmisc.undo;
	  requires wellbehavedfx;

    exports org.fxmisc.richtext;
    exports org.fxmisc.richtext.event;
    exports org.fxmisc.richtext.model;
    exports org.fxmisc.richtext.util;

}
EOF

echo "[10/13] 编译 RichTextFX module-info"
javac \
  --module-path "$JAVAFX_LIB:$REACTFX_JAR:$FLOWLESS_JAR:$WELLBEHAVEDFX_JAR:$UNDOFX_JAR" \
  --patch-module org.fxmisc.richtext="$RICHTEXTFX_JAR" \
  -d richtextfx-out \
  org.fxmisc.richtext/module-info.java

echo "更新 RichTextFX JAR"
jar --update --file="$RICHTEXTFX_JAR" -C richtextfx-out module-info.class

echo "安装 RichTextFX 到本地 Maven 仓库"
mvn install:install-file \
  -Dfile="$RICHTEXTFX_JAR" \
  -DgroupId=org.fxmisc.richtext \
  -DartifactId=richtextfx \
  -Dversion=0.11.5-modular \
  -Dpackaging=jar
# ================RichTextFX End=====================
echo ""
echo "🎉 所有模块化依赖均已安装完成。"
