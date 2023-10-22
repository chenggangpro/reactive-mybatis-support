/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package pro.chenggang.project.reactive.mybatis.support.r2dbc.execution.type.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SpecificEnumType {
    A(1),
    B(2) {
        @Override
        public String getName() {
            return "a";
        }
    },
    C(3),
    ;

    public String getName() {
        return this.name();
    }

    private final int value;

    public static SpecificEnumType fromValue(int i) {
        for (SpecificEnumType t : values()) {
            if (t.value == i) {
                return t;
            }
        }
        return null;
    }
}