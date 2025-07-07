#version 330 core

layout (location=0) in vec3 position;

uniform mat4 projectionView;

out vec3 vertTextureCoords;

void main() {
	vertTextureCoords = position;
	vec4 staticPosition = projectionView * vec4(position, 1.0);
	gl_Position = staticPosition.xyww;
}