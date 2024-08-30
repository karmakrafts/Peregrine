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

package io.karma.peregrine.uniform;

import io.karma.peregrine.uniform.MatrixUniform.*;
import io.karma.peregrine.uniform.ScalarUniform.DoubleUniform;
import io.karma.peregrine.uniform.ScalarUniform.FloatUniform;
import io.karma.peregrine.uniform.ScalarUniform.IntUniform;
import io.karma.peregrine.uniform.ScalarUniform.LongUniform;
import io.karma.peregrine.uniform.VectorUniform.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BiFunction;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultUniformTypeFactories implements UniformTypeFactories {
    @Override
    public BiFunction<String, Object, IntUniform> getInt() {
        return DefaultIntUniform::new;
    }

    @Override
    public BiFunction<String, Object, LongUniform> getLong() {
        return DefaultLongUniform::new;
    }

    @Override
    public BiFunction<String, Object, FloatUniform> getFloat() {
        return DefaultFloatUniform::new;
    }

    @Override
    public BiFunction<String, Object, DoubleUniform> getDouble() {
        return DefaultDoubleUniform::new;
    }

    @Override
    public BiFunction<String, Object, Vec2Uniform> getVec2() {
        return DefaultVec2Uniform::new;
    }

    @Override
    public BiFunction<String, Object, Vec3Uniform> getVec3() {
        return DefaultVec3Uniform::new;
    }

    @Override
    public BiFunction<String, Object, Vec4Uniform> getVec4() {
        return DefaultVec4Uniform::new;
    }

    @Override
    public BiFunction<String, Object, IVec2Uniform> getIVec2() {
        return DefaultIVec2Uniform::new;
    }

    @Override
    public BiFunction<String, Object, IVec3Uniform> getIVec3() {
        return DefaultIVec3Uniform::new;
    }

    @Override
    public BiFunction<String, Object, IVec4Uniform> getIVec4() {
        return DefaultIVec4Uniform::new;
    }

    @Override
    public BiFunction<String, Object, DVec2Uniform> getDVec2() {
        return DefaultDVec2Uniform::new;
    }

    @Override
    public BiFunction<String, Object, DVec3Uniform> getDVec3() {
        return DefaultDVec3Uniform::new;
    }

    @Override
    public BiFunction<String, Object, DVec4Uniform> getDVec4() {
        return DefaultDVec4Uniform::new;
    }

    @Override
    public BiFunction<String, Object, Mat2Uniform> getMat2() {
        return DefaultMat2Uniform::new;
    }

    @Override
    public BiFunction<String, Object, Mat3Uniform> getMat3() {
        return DefaultMat3Uniform::new;
    }

    @Override
    public BiFunction<String, Object, Mat4Uniform> getMat4() {
        return DefaultMat4Uniform::new;
    }

    @Override
    public BiFunction<String, Object, DMat2Uniform> getDMat2() {
        return DefaultDMat2Uniform::new;
    }

    @Override
    public BiFunction<String, Object, DMat3Uniform> getDMat3() {
        return DefaultDMat3Uniform::new;
    }

    @Override
    public BiFunction<String, Object, DMat4Uniform> getDMat4() {
        return DefaultDMat4Uniform::new;
    }
}
