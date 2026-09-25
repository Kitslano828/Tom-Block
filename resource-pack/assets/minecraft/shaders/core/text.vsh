#version 330

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:sample_lightmap.glsl>
#endif
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
in ivec2 UV2;
#endif
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
uniform sampler2D Sampler2;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
#endif
out vec4 vertexColor;
out vec2 texCoord0;

vec4 hudColor(int encodedGreen) {
    int palette = (encodedGreen - 16) / 9;
    if (palette == 0) return vec4(1.00000000, 1.00000000, 1.00000000, 1.0);
    if (palette == 1) return vec4(1.00000000, 0.25098039, 0.33333333, 1.0);
    if (palette == 2) return vec4(0.00000000, 0.90196078, 0.76470588, 1.0);
    if (palette == 3) return vec4(0.94901961, 0.68235294, 0.19607843, 1.0);
    if (palette == 4) return vec4(0.66666667, 0.66666667, 0.66666667, 1.0);
    if (palette == 5) return vec4(1.00000000, 0.66666667, 0.00000000, 1.0);
    if (palette == 6) return vec4(1.00000000, 0.33333333, 0.33333333, 1.0);
    if (palette == 7) return vec4(1.00000000, 1.00000000, 0.33333333, 1.0);
    if (palette == 8) return vec4(0.33333333, 1.00000000, 0.33333333, 1.0);
    if (palette == 9) return vec4(0.00000000, 0.66666667, 0.66666667, 1.0);
    if (palette == 10) return vec4(0.33333333, 0.33333333, 0.33333333, 1.0);
    return vec4(1.0);
}

void main() {
    vec3 hudPosition = Position;
    ivec3 marker = ivec3(round(Color.rgb * 255.0));
    bool tomblockHud = marker.r == 250 && marker.g >= 16 && marker.g <= 159;
    if (tomblockHud) {
        vec2 screen = vec2(2.0 / ProjMat[0][0], -2.0 / ProjMat[1][1]);
		int encoded = marker.g - 16;
		int anchor = encoded % 9;
		int palette = encoded / 9;
        int horizontal = anchor % 3;
        int vertical = anchor / 3;
        if (horizontal == 0) hudPosition.x -= screen.x * 0.5;
        else if (horizontal == 2) hudPosition.x += screen.x * 0.5;
        float localY = Position.y - (screen.y - 59.0 - 7.0);
        float anchorY = vertical == 0 ? 0.0 : (vertical == 1 ? screen.y * 0.5 : screen.y);
        hudPosition.y = anchorY + float(marker.b - 128) + localY;
    }
    gl_Position = ProjMat * ModelViewMat * vec4(hudPosition, 1.0);
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = tomblockHud ? hudColor(marker.g) : Color * sample_lightmap(Sampler2, UV2);
#else
    vertexColor = tomblockHud ? hudColor(marker.g) : Color;
#endif
    texCoord0 = UV0;
}
