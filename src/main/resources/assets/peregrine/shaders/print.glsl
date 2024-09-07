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

#if (BUILTIN_SSBO_SUPPORT == 1)
#if (BUILTIN_GL_MAJOR < 4 || (BUILTIN_GL_MAJOR == 4 && BUILTIN_GL_MINOR < 3))
#if (BUILTIN_SHADER_TYPE == SHADER_TYPE_VERTEX)
#extension GL_ARB_shader_storage_buffer_object : require
#define BUILTIN_PRINT_SUPPORT 1
#else//BUILTIN_SHADER_TYPE
#define BUILTIN_PRINT_SUPPORT 0
#endif//BUILTIN_SHADER_TYPE
#else
#extension GL_ARB_shader_storage_buffer_object : require
#define BUILTIN_PRINT_SUPPORT 1
#endif
#endif//BUILTIN_SSBO_SUPPORT

special const int PRINT_BUFFER_SIZE = 1;

#if (BUILTIN_PRINT_SUPPORT == 1)
layout(std430) buffer PrintBuffer {
    uint data[PRINT_BUFFER_SIZE];
};
#endif//BUILTIN_PRINT_SUPPORT