/*
 * Copyright (c) 2018, 2018, Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.truffle.regex.tregex.matchers;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ProfilingCharMatcher extends CharMatcher {

    @Child private CharMatcher byteMatcher;
    @Child private CharMatcher charMatcher;

    ProfilingCharMatcher(CharMatcher byteMatcher, CharMatcher charMatcher) {
        this.byteMatcher = byteMatcher;
        this.charMatcher = charMatcher;
    }

    public static ProfilingCharMatcher create(CharMatcher byteMatcher, CharMatcher charMatcher) {
        return ProfilingCharMatcherNodeGen.create(byteMatcher, charMatcher);
    }

    @Specialization(guards = "compactString")
    boolean matchCompactString(char c, boolean compactString) {
        return byteMatcher.execute(c, compactString);
    }

    @Specialization(guards = {"!compactString", "isByte(c)"})
    boolean matchByte(char c, boolean compactString) {
        return byteMatcher.execute(c, compactString);
    }

    @Specialization(guards = "!compactString", replaces = "matchByte")
    boolean matchChar(char c, boolean compactString) {
        return charMatcher.execute(c, compactString);
    }

    static boolean isByte(char c) {
        return c < 256;
    }

    @Override
    public int estimatedCost() {
        return charMatcher.estimatedCost();
    }

    @TruffleBoundary
    @Override
    public String toString() {
        return charMatcher.toString();
    }
}
