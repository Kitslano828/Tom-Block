#version 330

// Opt-in experiment: remove dark translucent GUI fills only in the
// upper-right scoreboard area. Textured map glyphs use another shader.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

in vec4 vertexColor;
in vec2 clipPosition;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor * ColorModulator;
    bool scoreboardRegion = clipPosition.x > 0.50 && clipPosition.y > -0.20;
    bool darkTranslucentFill = color.a > 0.10 && color.a < 0.90
        && color.r < 0.15 && color.g < 0.15 && color.b < 0.15;
    if (scoreboardRegion && darkTranslucentFill) {
        discard;
    }
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color;
}
