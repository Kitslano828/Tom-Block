#version 330

// Adapted from the supplied 1.20 RemoveScoreboardBG pack for 26.2.
// This is an opt-in experiment: the selected top-right GUI region may also
// contain unrelated overlays at the same depth.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

in vec3 Position;
in vec4 Color;

out vec4 vertexColor;
out vec2 clipPosition;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    clipPosition = gl_Position.xy / gl_Position.w;
}
