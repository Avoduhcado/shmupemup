#version 330 core

layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform mat3 normalMatrix;

out vec3 TexCoord0;

void main()
{
	gl_Position = projection * view * model * vec4(position, 1.0);
//	mat3 normMatrix = transpose(inverse(mat3(view * model)));
//	TexCoord0 = normalize(normalMatrix * normal);
	TexCoord0 = normal;
}