#version 330 core

in vec3 TexCoord0;

uniform samplerCube Texture0;

out vec4 fragColor;

void main() {
	fragColor = texture(Texture0, TexCoord0);
}