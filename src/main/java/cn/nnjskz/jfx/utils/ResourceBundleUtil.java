/*
 * jfx-network-tools - A lightweight JavaFX-based network debugging tool
 * Copyright (c) 2025 Jensen
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package cn.nnjskz.jfx.utils;

import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * 资源束工具类
 * @author Jensen
 */
public class ResourceBundleUtil {
    private static final ResourceBundle CONFIG_BUNDLE = ResourceBundle.getBundle("config/config");

    public static final Function<String,String> getProperty = k -> {
        try{
            if(CONFIG_BUNDLE.containsKey(k))
                return CONFIG_BUNDLE.getString(k);
            else throw new NullPointerException("NullPointerException:can't find properties key:【"+k+"】 -Jensen");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    };
}
