#version 330 core

layout (location=0) in vec3 position;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

out vec3 TexCoord0;

void main()
{
	TexCoord0 = position;
	
	gl_Position = projection * view * model * vec4(position, 1.0);
}