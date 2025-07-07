#version 330 core

layout(points) in;
layout(triangle_strip, max_vertices = 16) out;

const float offset = 0.2;
const float midpoint = 0.025;
const vec4 offsetColor = vec4(1.0);
const vec4 midpointColor = vec4(1.0, 1.0, 1.0, 0.0);

out vec4 gColor;

void drawBottomSegment() {
	gl_Position = gl_in[0].gl_Position + vec4(-midpoint, -midpoint, 0.0, 0.0);
	gColor = midpointColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position;
	gColor = offsetColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + vec4(0.0, -offset, 0.0, 0.0);
	gColor = offsetColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + vec4(midpoint, -midpoint, 0.0, 0.0);
	gColor = midpointColor;
	EmitVertex();
	
	EndPrimitive();
}

void drawRightSegment() {
	gl_Position = gl_in[0].gl_Position + vec4(midpoint, -midpoint, 0.0, 0.0);
	gColor = midpointColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position;
	gColor = offsetColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + vec4(offset, 0.0, 0.0, 0.0);
	gColor = offsetColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + vec4(midpoint, midpoint, 0.0, 0.0);
	gColor = midpointColor;
	EmitVertex();
	
	EndPrimitive();
}

void drawTopSegment() {
	gl_Position = gl_in[0].gl_Position + vec4(midpoint, midpoint, 0.0, 0.0);
	gColor = midpointColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position;
	gColor = offsetColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + vec4(0.0, offset, 0.0, 0.0);
	gColor = offsetColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + vec4(-midpoint, midpoint, 0.0, 0.0);
	gColor = midpointColor;
	EmitVertex();
	
	EndPrimitive();
}

void drawLeftSegment() {
	gl_Position = gl_in[0].gl_Position + vec4(-midpoint, -midpoint, 0.0, 0.0);
	gColor = midpointColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position;
	gColor = offsetColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + vec4(-offset, 0.0, 0.0, 0.0);
	gColor = offsetColor;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + vec4(-midpoint, midpoint, 0.0, 0.0);
	gColor = midpointColor;
	EmitVertex();
	
	EndPrimitive();
}

void main() {
	drawBottomSegment();
	drawRightSegment();
	drawTopSegment();
	drawLeftSegment();
}