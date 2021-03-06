/*
 * Copyright (c) 2017, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.jdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.impl.InternalPlatform;
import org.graalvm.word.PointerBase;

public abstract class PlatformNativeLibrarySupport {

    public static final String[] defaultBuiltInLibraries = {
                    "java",
                    "nio",
                    "net",
                    "zip"
    };

    private static final String[] defaultBuiltInPkgNatives = {
                    "com_sun_demo_jvmti_hprof",
                    "com_sun_java_util_jar_pack",
                    "com_sun_net_ssl",
                    "com_sun_nio_file",
                    "com_sun_security_cert_internal_x509",
                    "java_io",
                    "java_lang",
                    "java_math",
                    "java_net",
                    "java_nio",
                    "java_security",
                    "java_text",
                    "java_time",
                    "java_util",
                    "javax_net",
                    "javax_script",
                    "javax_security",
                    "jdk_internal_org",
                    "jdk_internal_util",
                    "jdk_net",
                    "sun_invoke",
                    "sun_launcher",
                    "sun_misc",
                    "sun_net",
                    "sun_nio",
                    "sun_reflect",
                    "sun_text",
                    "sun_util",

                    /* SVM Specific packages */
                    "com_oracle_svm_core_jdk"
    };

    private static final String[] defaultBuiltInPkgNativesBlacklist = {
                    "sun_security_krb5_SCDynamicStoreConfig_getKerberosConfig",
                    "sun_security_krb5_Config_getWindowsDirectory"
    };

    public static PlatformNativeLibrarySupport singleton() {
        return ImageSingletons.lookup(PlatformNativeLibrarySupport.class);
    }

    protected PlatformNativeLibrarySupport() {
        builtInPkgNatives = new ArrayList<>();
        if (Platform.includedIn(InternalPlatform.PLATFORM_JNI.class)) {
            builtInPkgNatives.addAll(Arrays.asList(defaultBuiltInPkgNatives));
        }
    }

    private List<String> builtInLibraries = new ArrayList<>(Arrays.asList(defaultBuiltInLibraries));

    public void addBuiltInLibrary(String libName) {
        builtInLibraries.add(libName);
    }

    public boolean isBuiltinLibrary(String name) {
        return builtInLibraries.contains(name);
    }

    private List<String> builtInPkgNatives;

    public void addBuiltinPkgNativePrefix(String name) {
        builtInPkgNatives.add(name);
    }

    public boolean isBuiltinPkgNative(String name) {
        String commonPrefix = "Java_";
        if (name.startsWith(commonPrefix)) {
            String strippedName = name.substring(commonPrefix.length());
            for (String str : defaultBuiltInPkgNativesBlacklist) {
                if (strippedName.startsWith(str)) {
                    return false;
                }
            }
            for (String str : builtInPkgNatives) {
                if (strippedName.startsWith(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface NativeLibrary {

        String getCanonicalIdentifier();

        boolean isBuiltin();

        boolean load();

        PointerBase findSymbol(String name);

    }

    public abstract NativeLibrary createLibrary(String canonical, boolean builtIn);

    public abstract PointerBase findBuiltinSymbol(String name);

    /**
     * Initializes built-in libraries that are explicitly or implicitly shared between the isolates
     * of a process (for example, because they have a single native state that does not distinguish
     * between isolate). This method is called exactly once per process, during the creation of the
     * first isolate in the process, followed by a call to {@link #initializeBuiltinLibraries()}.
     */
    public boolean initializeSharedBuiltinLibrariesOnce() {
        return true;
    }

    /** Initializes built-in libraries for the current isolate, during its creation. */
    public abstract boolean initializeBuiltinLibraries();
}
