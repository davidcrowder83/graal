/*
 * Copyright (c) 2016, 2019, Oracle and/or its affiliates.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.truffle.llvm.runtime.nodes.intrinsics.llvm.arith;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.llvm.runtime.nodes.memory.store.LLVMDoubleStoreNode;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMExpressionNode;
import com.oracle.truffle.llvm.runtime.nodes.memory.store.LLVMDoubleStoreNodeGen;
import com.oracle.truffle.llvm.runtime.pointer.LLVMPointer;

public final class LLVMComplexDoubleMul extends LLVMExpressionNode {

    @Child private LLVMExpressionNode aNode;
    @Child private LLVMExpressionNode bNode;
    @Child private LLVMExpressionNode cNode;
    @Child private LLVMExpressionNode dNode;
    @Child private LLVMExpressionNode alloc;

    @Child private LLVMDoubleStoreNode store;

    public LLVMComplexDoubleMul(LLVMExpressionNode alloc, LLVMExpressionNode a, LLVMExpressionNode b, LLVMExpressionNode c, LLVMExpressionNode d) {
        this.alloc = alloc;
        this.aNode = a;
        this.bNode = b;
        this.cNode = c;
        this.dNode = d;

        this.store = LLVMDoubleStoreNodeGen.create(null, null);
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        try {
            double a = aNode.executeDouble(frame);
            double b = bNode.executeDouble(frame);
            double c = cNode.executeDouble(frame);
            double d = dNode.executeDouble(frame);

            double ac = a * c;
            double bd = b * d;
            double ad = a * d;
            double bc = b * c;
            double zReal = ac - bd;
            double zImag = ad + bc;

            LLVMPointer allocatedMemory = alloc.executeLLVMPointer(frame);
            store.executeWithTarget(allocatedMemory, zReal);
            store.executeWithTarget(allocatedMemory.increment(LLVMExpressionNode.DOUBLE_SIZE_IN_BYTES), zImag);

            return allocatedMemory;
        } catch (UnexpectedResultException e) {
            CompilerDirectives.transferToInterpreter();
            throw new IllegalStateException(e);
        }
    }
}
