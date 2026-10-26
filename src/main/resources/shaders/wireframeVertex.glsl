#version 330 core

layout (location=0) in vec3 position;
layout (location=1) in vec3 center;
layout (location=2) in vec3 scale;

uniform mat4 projectionViewMatrix;

void main() {
	
	vec3 worldPos = (position * 2.0 * scale) + center;
	gl_Position = projectionViewMatrix * vec4(worldPos, 1.0);
	
}