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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface VectorUniform<T> extends Uniform<T> {
    interface Vec2Uniform extends VectorUniform<Vector2f> {
        @Override
        default UniformType getType() {
            return VectorType.VEC2;
        }
    }

    interface Vec3Uniform extends VectorUniform<Vector3f> {
        @Override
        default UniformType getType() {
            return VectorType.VEC3;
        }
    }

    interface Vec4Uniform extends VectorUniform<Vector4f> {
        @Override
        default UniformType getType() {
            return VectorType.VEC4;
        }
    }

    interface IVec2Uniform extends VectorUniform<Vector2i> {
        @Override
        default UniformType getType() {
            return VectorType.IVEC2;
        }
    }

    interface IVec3Uniform extends VectorUniform<Vector3i> {
        @Override
        default UniformType getType() {
            return VectorType.IVEC3;
        }
    }

    interface IVec4Uniform extends VectorUniform<Vector4i> {
        @Override
        default UniformType getType() {
            return VectorType.IVEC4;
        }
    }

    interface DVec2Uniform extends VectorUniform<Vector2d> {
        @Override
        default UniformType getType() {
            return VectorType.DVEC2;
        }
    }

    interface DVec3Uniform extends VectorUniform<Vector3d> {
        @Override
        default UniformType getType() {
            return VectorType.DVEC3;
        }
    }

    interface DVec4Uniform extends VectorUniform<Vector4d> {
        @Override
        default UniformType getType() {
            return VectorType.DVEC4;
        }
    }
}
