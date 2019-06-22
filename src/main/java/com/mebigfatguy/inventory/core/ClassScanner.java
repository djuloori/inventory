/*
 * inventory - an ear/war inventory recorder
 * Copyright 2016-2019 MeBigFatGuy.com
 * Copyright 2016-2019 Dave Brosius
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package com.mebigfatguy.inventory.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import com.mebigfatguy.inventory.cls.ClassInventoryVisitor;

public class ClassScanner implements ArchiveScanner {

    @Override
    public void scan(String name, Inventory inventory) throws IOException {

        try (InputStream is = inventory.getStream()) {
            ClassInventoryVisitor visitor = new ClassInventoryVisitor(inventory);
            ClassReader cr = new ClassReader(is);
            cr.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            Set<String> classDependencies = getClassDependencies(cr);
            classDependencies.forEach(System.out::println);
        }
    }
    
    private Set<String> getClassDependencies(ClassReader reader) {
        Set<String> dependencies = new HashSet<String>();
        char[] charBuffer = new char[reader.getMaxStringLength()];
        for (int i = 1; i < reader.getItemCount(); i++) {
            int itemOffset = reader.getItem(i);
            if (itemOffset > 0 && reader.readByte(itemOffset - 1) == 7) {
                String classDescriptor = reader.readUTF8(itemOffset, charBuffer);
                Type type = Type.getObjectType(classDescriptor);
                while (type.getSort() == Type.ARRAY) {
                    type = type.getElementType();
                }
                if (type.getSort() != Type.OBJECT) {
                    continue;
                }
                String name = type.getClassName();
                dependencies.add(name);
            }
        }
        return dependencies;
    }

}
