#version 330 core

uniform vec4 wireframeColor;

out vec4 fragColor;

void main() {

	fragColor = wireframeColor;

}