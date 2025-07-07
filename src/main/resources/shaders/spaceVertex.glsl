#version 330 core

layout (location=0) in vec3 position;

uniform mat4 projection;
uniform mat4 view;

void main() {

	vec4 pos = projection * mat4(mat3(view)) * vec4(position, 1.0);
//	vec4 pos = projection * view * vec4(position, 1.0);
//	gl_Position = pos.xyww;
	gl_Position = pos;

}