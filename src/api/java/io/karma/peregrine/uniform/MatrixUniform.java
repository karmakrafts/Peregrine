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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface MatrixUniform<T> extends Uniform<T> {
    interface Mat2Uniform extends MatrixUniform<Matrix2f> {
        default @Override UniformType getType() {
            return MatrixType.MAT2;
        }
    }

    interface Mat3Uniform extends MatrixUniform<Matrix3f> {
        default @Override UniformType getType() {
            return MatrixType.MAT3;
        }
    }

    interface Mat4Uniform extends MatrixUniform<Matrix4f> {
        default @Override UniformType getType() {
            return MatrixType.MAT4;
        }
    }

    interface DMat2Uniform extends MatrixUniform<Matrix2d> {
        default @Override UniformType getType() {
            return MatrixType.DMAT2;
        }
    }

    interface DMat3Uniform extends MatrixUniform<Matrix3d> {
        default @Override UniformType getType() {
            return MatrixType.DMAT3;
        }
    }

    interface DMat4Uniform extends MatrixUniform<Matrix4d> {
        default @Override UniformType getType() {
            return MatrixType.DMAT4;
        }
    }
}
