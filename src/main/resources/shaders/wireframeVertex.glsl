#version 330 core

layout (location=0) in vec3 position;

uniform mat4 projectionViewMatrix;

void main() {
	
	gl_Position = projectionViewMatrix * vec4(position, 1.0);
	
}