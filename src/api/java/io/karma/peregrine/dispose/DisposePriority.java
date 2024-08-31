/*
 * Copyright 2024 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.karma.peregrine.dispose;

import io.karma.peregrine.util.Dispatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runtime hint for the {@link io.karma.peregrine.dispose.DispositionHandler}
 * to overwrite an objects disposition priority without overwriting code
 * from {@link io.karma.peregrine.dispose.Disposable}.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DisposePriority {
    /**
     * @return the priority at which the annotated object should be disposed.
     */
    int value();

    /**
     * @return the dispatcher with which the annotated object should be disposed.
     */
    Dispatcher dispatcher() default Dispatcher.MAIN;
}
