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

import io.karma.peregrine.buffer.UniformBuffer;
import io.karma.peregrine.shader.ShaderProgram;
import io.karma.peregrine.uniform.MatrixUniform.*;
import io.karma.peregrine.uniform.ScalarUniform.DoubleUniform;
import io.karma.peregrine.uniform.ScalarUniform.FloatUniform;
import io.karma.peregrine.uniform.ScalarUniform.IntUniform;
import io.karma.peregrine.uniform.ScalarUniform.LongUniform;
import io.karma.peregrine.uniform.VectorUniform.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface UniformCache {
    void applyAll(final ShaderProgram program);

    void uploadAll(final UniformBuffer block);

    void updateAll();

    Map<String, ? extends Uniform<?>> getAll();

    Uniform<?> get(final String name);

    // Scalars

    default IntUniform getInt(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != ScalarType.INT) {
            throw new IllegalStateException(String.format("Uniform %s is not a int uniform", name));
        }
        return (IntUniform) uniform;
    }

    default LongUniform getLong(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != ScalarType.LONG) {
            throw new IllegalStateException(String.format("Uniform %s is not a long uniform", name));
        }
        return (LongUniform) uniform;
    }

    default FloatUniform getFloat(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != ScalarType.FLOAT) {
            throw new IllegalStateException(String.format("Uniform %s is not a float uniform", name));
        }
        return (FloatUniform) uniform;
    }

    default DoubleUniform getDouble(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != ScalarType.DOUBLE) {
            throw new IllegalStateException(String.format("Uniform %s is not a double uniform", name));
        }
        return (DoubleUniform) uniform;
    }

    default Vec2Uniform getVec2(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != VectorType.VEC2) {
            throw new IllegalStateException(String.format("Uniform %s is not a vec2 uniform", name));
        }
        return (Vec2Uniform) uniform;
    }

    default Vec3Uniform getVec3(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != VectorType.VEC3) {
            throw new IllegalStateException(String.format("Uniform %s is not a vec3 uniform", name));
        }
        return (Vec3Uniform) uniform;
    }

    default Vec4Uniform getVec4(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != VectorType.VEC4) {
            throw new IllegalStateException(String.format("Uniform %s is not a vec4 uniform", name));
        }
        return (Vec4Uniform) uniform;
    }

    default IVec2Uniform getIVec2(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != VectorType.IVEC2) {
            throw new IllegalStateException(String.format("Uniform %s is not a ivec2 uniform", name));
        }
        return (IVec2Uniform) uniform;
    }

    default IVec3Uniform getIVec3(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != VectorType.IVEC3) {
            throw new IllegalStateException(String.format("Uniform %s is not a ivec3 uniform", name));
        }
        return (IVec3Uniform) uniform;
    }

    default IVec4Uniform getIVec4(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != VectorType.IVEC4) {
            throw new IllegalStateException(String.format("Uniform %s is not a ivec4 uniform", name));
        }
        return (IVec4Uniform) uniform;
    }

    default DVec2Uniform getDVec2(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != VectorType.DVEC2) {
            throw new IllegalStateException(String.format("Uniform %s is not a dvec2 uniform", name));
        }
        return (DVec2Uniform) uniform;
    }

    default DVec3Uniform getDVec3(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != VectorType.DVEC3) {
            throw new IllegalStateException(String.format("Uniform %s is not a dvec3 uniform", name));
        }
        return (DVec3Uniform) uniform;
    }

    default DVec4Uniform getDVec4(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != VectorType.DVEC4) {
            throw new IllegalStateException(String.format("Uniform %s is not a dvec4 uniform", name));
        }
        return (DVec4Uniform) uniform;
    }

    default Mat2Uniform getMat2(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != MatrixType.MAT2) {
            throw new IllegalStateException(String.format("Uniform %s is not a mat2 uniform", name));
        }
        return (Mat2Uniform) uniform;
    }

    default Mat3Uniform getMat3(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != MatrixType.MAT3) {
            throw new IllegalStateException(String.format("Uniform %s is not a mat3 uniform", name));
        }
        return (Mat3Uniform) uniform;
    }

    default Mat4Uniform getMat4(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != MatrixType.MAT4) {
            throw new IllegalStateException(String.format("Uniform %s is not a mat4 uniform", name));
        }
        return (Mat4Uniform) uniform;
    }

    default DMat2Uniform getDMat2(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != MatrixType.DMAT2) {
            throw new IllegalStateException(String.format("Uniform %s is not a dmat2 uniform", name));
        }
        return (DMat2Uniform) uniform;
    }

    default DMat3Uniform getDMat3(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != MatrixType.DMAT3) {
            throw new IllegalStateException(String.format("Uniform %s is not a dmat3 uniform", name));
        }
        return (DMat3Uniform) uniform;
    }

    default DMat4Uniform getDMat4(final String name) {
        final var uniform = get(name);
        if (uniform.getType() != MatrixType.DMAT4) {
            throw new IllegalStateException(String.format("Uniform %s is not a dmat4 uniform", name));
        }
        return (DMat4Uniform) uniform;
    }
}
