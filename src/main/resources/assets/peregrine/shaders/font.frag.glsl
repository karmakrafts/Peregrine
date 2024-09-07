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

#version 410 core

#include "globals.glsl"

uniform sampler2D Sampler0;

uniform float PxRange;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float median(vec3 rgb) {
    return max(min(rgb.r, rgb.g), min(max(rgb.r, rgb.g), rgb.b));
}

void main() {
    float sd = PxRange * (median(texture(Sampler0, texCoord0).rgb) - 0.5);
    float opacity = clamp(sd + 0.5, 0.0, 1.0);
    fragColor = mix(vec4(0.0), vertexColor * ColorModulator, opacity);
}