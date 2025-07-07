#version 330 core

in vec3 vertTextureCoords;

uniform samplerCube starmap;

out vec4 fragColor;

void main() {
	fragColor = texture(starmap, vertTextureCoords);
}