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

package io.karma.peregrine.api.uniform;

import io.karma.peregrine.api.uniform.MatrixUniform.*;
import io.karma.peregrine.api.uniform.ScalarUniform.DoubleUniform;
import io.karma.peregrine.api.uniform.ScalarUniform.FloatUniform;
import io.karma.peregrine.api.uniform.ScalarUniform.IntUniform;
import io.karma.peregrine.api.uniform.ScalarUniform.LongUniform;
import io.karma.peregrine.api.uniform.VectorUniform.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BiFunction;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface UniformTypeFactories {
    // @formatter:off
    BiFunction<String, Object, IntUniform> getInt();
    BiFunction<String, Object, LongUniform> getLong();
    BiFunction<String, Object, FloatUniform> getFloat();
    BiFunction<String, Object, DoubleUniform> getDouble();

    BiFunction<String, Object, Vec2Uniform> getVec2();
    BiFunction<String, Object, Vec3Uniform> getVec3();
    BiFunction<String, Object, Vec4Uniform> getVec4();
    BiFunction<String, Object, IVec2Uniform> getIVec2();
    BiFunction<String, Object, IVec3Uniform> getIVec3();
    BiFunction<String, Object, IVec4Uniform> getIVec4();
    BiFunction<String, Object, DVec2Uniform> getDVec2();
    BiFunction<String, Object, DVec3Uniform> getDVec3();
    BiFunction<String, Object, DVec4Uniform> getDVec4();

    BiFunction<String, Object, Mat2Uniform> getMat2();
    BiFunction<String, Object, Mat3Uniform> getMat3();
    BiFunction<String, Object, Mat4Uniform> getMat4();
    BiFunction<String, Object, DMat2Uniform> getDMat2();
    BiFunction<String, Object, DMat3Uniform> getDMat3();
    BiFunction<String, Object, DMat4Uniform> getDMat4();
    // @formatter:on
}
